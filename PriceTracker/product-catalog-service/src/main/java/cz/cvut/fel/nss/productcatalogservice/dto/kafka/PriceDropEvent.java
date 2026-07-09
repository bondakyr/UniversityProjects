package cz.cvut.fel.nss.productcatalogservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDropEvent {
    private String eventId;
    private Long productId;
    private String productName;
    private Long userId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private BigDecimal thresholdValue;
    private String shopName;
    private String productUrl;
    private Long triggeredAt;
}
