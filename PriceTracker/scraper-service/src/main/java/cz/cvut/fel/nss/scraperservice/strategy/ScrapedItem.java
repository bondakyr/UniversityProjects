package cz.cvut.fel.nss.scraperservice.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class ScrapedItem {
    private Long productId;
    private String productExternalId;
    private String productName;
    private BigDecimal price;
    private Integer stockCount;
    private String productUrl;
}
