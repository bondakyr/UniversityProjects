package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceRecord;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceRecordRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
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
class PriceHistoryExportServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private PriceRecordRepository priceRecordRepository;

    @InjectMocks
    private PriceHistoryExportService service;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("RAM 32GB");
    }

    private PriceRecord record(BigDecimal price, String shop, Integer stock, String url) {
        PriceRecord r = new PriceRecord();
        r.setPrice(price);
        r.setShopName(shop);
        r.setStockCount(stock);
        r.setProductUrl(url);
        r.setScrapedAt(LocalDateTime.of(2026, 1, 15, 10, 30));
        return r;
    }

    @Test
    void export_writesHeaderAndRows() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(priceRecordRepository.findByProductIdOrderByScrapedAtAsc(1L))
                .thenReturn(List.of(record(new BigDecimal("1999.00"), "Alza", 5, "https://alza.cz/ram")));

        String csv = service.exportAsCsv(1L);
        String[] lines = csv.split("\n");

        assertThat(lines[0]).isEqualTo("product_id,product_name,scraped_at,shop_name,price,stock_count,product_url");
        assertThat(lines[1]).contains("1", "RAM 32GB", "Alza", "1999.00", "5", "https://alza.cz/ram");
    }

    @Test
    void export_escapesFieldsContainingCommas() {
        product.setName("RAM, 32GB");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(priceRecordRepository.findByProductIdOrderByScrapedAtAsc(1L))
                .thenReturn(List.of(record(new BigDecimal("10"), "Shop A", null, "url")));

        String csv = service.exportAsCsv(1L);

        assertThat(csv).contains("\"RAM, 32GB\"");
    }

    @Test
    void export_leavesStockEmpty_whenNull() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(priceRecordRepository.findByProductIdOrderByScrapedAtAsc(1L))
                .thenReturn(List.of(record(new BigDecimal("10"), "Alza", null, "url")));

        String csv = service.exportAsCsv(1L);
        String dataRow = csv.split("\n")[1];

        assertThat(dataRow).contains("10,,url");
    }

    @Test
    void export_throws_whenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exportAsCsv(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
