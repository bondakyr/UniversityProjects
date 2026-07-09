package cz.cvut.fel.nss.productcatalogservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateEvent {
    private Long productId;
    private BigDecimal price;
    private String shopName;
    private String productUrl;
    private String productExternalId;
    private String productName;
    private Integer stockCount;
}
