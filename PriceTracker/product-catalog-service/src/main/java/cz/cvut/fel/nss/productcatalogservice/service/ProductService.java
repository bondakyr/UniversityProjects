package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.productcatalogservice.dto.catalog.PriceHistoryResponse;
import cz.cvut.fel.nss.productcatalogservice.dto.catalog.ProductCreateRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.catalog.ProductDetailResponse;
import cz.cvut.fel.nss.productcatalogservice.dto.catalog.ProductSummaryResponse;
import cz.cvut.fel.nss.productcatalogservice.dto.catalog.ScrapeTargetResponse;
import cz.cvut.fel.nss.productcatalogservice.dto.kafka.PriceUpdateEvent;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceRecord;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.kafka.PriceUpdateProducer;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceRecordRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
import cz.cvut.fel.nss.productcatalogservice.search.ProductSearchService;
import cz.cvut.fel.nss.productcatalogservice.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import cz.cvut.fel.nss.productcatalogservice.entity.Category;
import cz.cvut.fel.nss.productcatalogservice.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final PriceRecordRepository priceRecordRepository;
    private final CategoryRepository categoryRepository;
    private final CommentService commentService;
    private final ProductSearchService productSearchService;
    private final PriceUpdateProducer priceUpdateProducer;

    @Cacheable("products")
    public Page<ProductSummaryResponse> getProducts(String name,
                                                    Long categoryId,
                                                    String shopName,
                                                    BigDecimal minPrice,
                                                    BigDecimal maxPrice,
                                                    int page,
                                                    int size) {
        if (canUseElasticSearch(name, categoryId, shopName, minPrice, maxPrice)) {
            Page<ProductSummaryResponse> indexedResults = productSearchService.searchByName(name, page, size)
                    .orElse(null);
            if (indexedResults != null && indexedResults.hasContent()) {
                return indexedResults;
            }
        }

        return productRepository.search(name, categoryId, shopName, minPrice, maxPrice, PageRequest.of(page, size))
                .map(this::mapToSummary);
    }

    public List<ScrapeTargetResponse> getScrapeTargets() {
        List<ScrapeTargetResponse> targets = new java.util.ArrayList<>();
        for (Product p : productRepository.findAll()) {
            if (p.getSourceUrl() == null || p.getSourceUrl().isBlank()) {
                continue;
            }
            targets.add(ScrapeTargetResponse.builder()
                    .productId(p.getId())
                    .productUrl(p.getSourceUrl())
                    .shopName(p.getSourceShop())
                    .build());
        }
        return targets;
    }

    @CacheEvict(value = CacheConfig.PRODUCT_DETAILS_CACHE, key = "#productId")
    public void evictProductDetail(Long productId) {
    }

    @Cacheable(value = CacheConfig.PRODUCT_DETAILS_CACHE, key = "#id")
    public ProductDetailResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<PriceRecord> history = priceRecordRepository.findByProductIdOrderByScrapedAtAsc(id);

        BigDecimal minPrice = history.stream()
                .map(PriceRecord::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        PriceRecord lowestCurrent = lowestCurrentRecord(history);
        BigDecimal currentLowest = lowestCurrent != null ? lowestCurrent.getPrice() : null;

        List<PriceHistoryResponse> historyDtos = history.stream()
                .map(r -> new PriceHistoryResponse(r.getPrice(), r.getShopName(), r.getScrapedAt()))
                .collect(Collectors.toList());

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .parameters(product.getParameters())
                .sourceUrl(product.getSourceUrl())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .priceHistory(historyDtos)
                .comments(commentService.listForProduct(id))
                .minPrice(minPrice)
                .currentLowestPrice(currentLowest)
                .build();
    }

    private ProductSummaryResponse mapToSummary(Product p) {
        List<PriceRecord> records = priceRecordRepository.findByProductIdOrderByScrapedAtAsc(p.getId());
        PriceRecord lowest = lowestCurrentRecord(records);
        BigDecimal currentPrice = lowest != null ? lowest.getPrice() : null;

        return ProductSummaryResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .imageUrl(p.getImageUrl())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .currentPrice(currentPrice)
                .shopName(lowest != null ? lowest.getShopName() : null)
                .build();
    }

    /**
     * Lowest <em>current</em> price = the cheapest among the most recent record
     * of each shop (UC1: "nejnižší aktuální cena"). Records must be ordered by
     * scrapedAt ascending so the last write per shop is its latest price.
     */
    private PriceRecord lowestCurrentRecord(List<PriceRecord> recordsAsc) {
        Map<String, PriceRecord> latestPerShop = new LinkedHashMap<>();
        for (PriceRecord r : recordsAsc) {
            latestPerShop.put(r.getShopName() == null ? "" : r.getShopName(), r);
        }
        return latestPerShop.values().stream()
                .filter(r -> r.getPrice() != null)
                .min(Comparator.comparing(PriceRecord::getPrice))
                .orElse(null);
    }

    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        Category category = null;
        if (request.getCategoryName() != null) {
            category = categoryRepository.findByName(request.getCategoryName())
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setName(request.getCategoryName());
                        return categoryRepository.save(newCat);
                    });
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        product.setSourceUrl(request.getProductUrl());
        product.setSourceShop(request.getShopName());

        Product savedProduct = productRepository.save(product);
        String shopName = request.getInitialPrice() != null
                ? (request.getShopName() != null ? request.getShopName() : "Manual")
                : null;

        if (request.getInitialPrice() != null) {
            PriceRecord record = new PriceRecord();
            record.setPrice(request.getInitialPrice());
            record.setShopName(shopName);
            record.setProductUrl(request.getProductUrl());
            record.setProduct(savedProduct);

            priceRecordRepository.save(record);
        }
        productSearchService.indexProduct(savedProduct, request.getInitialPrice(), shopName);

        return getProductDetail(savedProduct.getId());
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
        productSearchService.deleteProduct(id); // Видаляємо з пошуку
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductCreateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getProductUrl() != null) product.setSourceUrl(request.getProductUrl());

        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            Category category = categoryRepository.findByName(request.getCategoryName())
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setName(request.getCategoryName());
                        return categoryRepository.save(newCat);
                    });
            product.setCategory(category);
        }

        if (request.getInitialPrice() != null) {
            PriceRecord record = new PriceRecord();
            record.setProduct(product);
            record.setPrice(request.getInitialPrice());
            record.setShopName(product.getSourceShop() != null ? product.getSourceShop() : "Manual");
            record.setProductUrl(product.getSourceUrl());
            priceRecordRepository.save(record);

            PriceUpdateEvent event = new PriceUpdateEvent();
            event.setProductId(product.getId());
            event.setPrice(request.getInitialPrice());
            event.setShopName(record.getShopName());
            event.setProductName(product.getName());
            priceUpdateProducer.sendPriceUpdate(event);

            productSearchService.indexProduct(product, request.getInitialPrice(), record.getShopName());
        }

        productRepository.save(product);
        return getProductDetail(id);
    }

    private boolean canUseElasticSearch(String name,
                                        Long categoryId,
                                        String shopName,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice) {
        return name != null && !name.isBlank()
                && categoryId == null
                && shopName == null
                && minPrice == null
                && maxPrice == null;
    }
}