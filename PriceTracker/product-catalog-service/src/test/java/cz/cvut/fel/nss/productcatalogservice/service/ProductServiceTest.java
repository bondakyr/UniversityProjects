package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.dto.catalog.ProductDetailResponse;
import cz.cvut.fel.nss.productcatalogservice.dto.catalog.ProductSummaryResponse;
import cz.cvut.fel.nss.productcatalogservice.entity.Category;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceRecord;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.repository.CategoryRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceRecordRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
import cz.cvut.fel.nss.productcatalogservice.search.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private PriceRecordRepository priceRecordRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CommentService commentService;
    @Mock
    private ProductSearchService productSearchService;

    @InjectMocks
    private ProductService service;

    private Product product;

    @BeforeEach
    void setUp() {
        Category cat = new Category();
        cat.setId(2L);
        cat.setName("RAM");

        product = new Product();
        product.setId(1L);
        product.setName("DDR5 32GB");
        product.setCategory(cat);
    }

    private PriceRecord record(BigDecimal price, int dayOffset) {
        PriceRecord r = new PriceRecord();
        r.setPrice(price);
        r.setShopName("Alza");
        r.setScrapedAt(LocalDateTime.of(2026, 1, 1, 0, 0).plusDays(dayOffset));
        return r;
    }

    @Test
    void getProductDetail_aggregatesHistoryMinAndCurrentLowestAndComments() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(priceRecordRepository.findByProductIdOrderByScrapedAtAsc(1L))
                .thenReturn(List.of(record(new BigDecimal("2500"), 0), record(new BigDecimal("1900"), 1)));
        when(priceRecordRepository.findTopByProductIdOrderByScrapedAtDesc(1L))
                .thenReturn(Optional.of(record(new BigDecimal("1900"), 1)));
        when(commentService.listForProduct(1L)).thenReturn(List.of());

        ProductDetailResponse detail = service.getProductDetail(1L);

        assertThat(detail.getName()).isEqualTo("DDR5 32GB");
        assertThat(detail.getCategoryName()).isEqualTo("RAM");
        assertThat(detail.getPriceHistory()).hasSize(2);
        assertThat(detail.getMinPrice()).isEqualByComparingTo("1900");
        assertThat(detail.getCurrentLowestPrice()).isEqualByComparingTo("1900");
        assertThat(detail.getComments()).isEmpty();
    }

    @Test
    void getProductDetail_throws_whenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProducts_usesElasticSearchForNameOnlyQueries() {
        var page = new org.springframework.data.domain.PageImpl<>(
                List.of(ProductSummaryResponse.builder().id(1L).name("DDR5 32GB").build()));
        when(productSearchService.searchByName("DDR5", 0, 10)).thenReturn(Optional.of(page));

        var result = service.getProducts("DDR5", null, null, null, null, 0, 10);

        assertThat(result).isSameAs(page);
    }
}
