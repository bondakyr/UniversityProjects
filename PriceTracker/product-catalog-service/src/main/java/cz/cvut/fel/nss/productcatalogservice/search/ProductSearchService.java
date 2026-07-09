package cz.cvut.fel.nss.productcatalogservice.search;

import cz.cvut.fel.nss.productcatalogservice.dto.catalog.ProductSummaryResponse;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {
    private static final String INDEX_NAME = "products";
    private final ElasticsearchClient elasticsearchClient;

    public Optional<Page<ProductSummaryResponse>> searchByName(String name, int page, int size) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        try {
            var response = elasticsearchClient.search(search -> search
                            .index(INDEX_NAME)
                            .query(query -> query.wildcard(wildcard -> wildcard
                                    .field("name")
                                    .value("*" + name.trim() + "*")))
                            .from(page * size)
                            .size(size),
                    ProductSearchDocument.class);

            List<ProductSummaryResponse> content = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(document -> document != null)
                    .map(this::toSummary)
                    .toList();

            long totalHits = response.hits().total() != null ? response.hits().total().value() : content.size();
            return Optional.of(new PageImpl<>(content, PageRequest.of(page, size), totalHits));
        } catch (Exception ex) {
            log.warn("Elasticsearch search failed, falling back to database search: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public void indexProduct(Product product, BigDecimal currentPrice, String currentShopName) {
        try {
            elasticsearchClient.index(index -> index
                    .index(INDEX_NAME)
                    .id(String.valueOf(product.getId()))
                    .document(toDocument(product, currentPrice, currentShopName)));
        } catch (Exception ex) {
            log.warn("Elasticsearch indexing failed for productId={}: {}", product.getId(), ex.getMessage());
        }
    }

    public void deleteProduct(Long productId) {
        try {
            elasticsearchClient.delete(delete -> delete
                    .index(INDEX_NAME)
                    .id(String.valueOf(productId)));
        } catch (Exception ex) {
            log.warn("Elasticsearch delete failed for productId={}: {}", productId, ex.getMessage());
        }
    }

    private ProductSummaryResponse toSummary(ProductSearchDocument document) {
        return ProductSummaryResponse.builder()
                .id(document.getId())
                .name(document.getName())
                .imageUrl(document.getImageUrl())
                .categoryName(document.getCategoryName())
                .currentPrice(document.getCurrentPrice() != null
                        ? BigDecimal.valueOf(document.getCurrentPrice())
                        : null)
                .shopName(document.getCurrentShopName())
                .build();
    }

    private ProductSearchDocument toDocument(Product product, BigDecimal currentPrice, String currentShopName) {
        return ProductSearchDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .currentPrice(currentPrice != null ? currentPrice.doubleValue() : null)
                .currentShopName(currentShopName)
                .updatedAt(Instant.now())
                .build();
    }
}
