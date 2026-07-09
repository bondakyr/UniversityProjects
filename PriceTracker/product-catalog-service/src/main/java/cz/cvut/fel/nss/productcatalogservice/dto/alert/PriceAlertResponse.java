package cz.cvut.fel.nss.productcatalogservice.dto.alert;

import cz.cvut.fel.nss.productcatalogservice.entity.PriceConditionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PriceAlertResponse {
    private Long id;
    private Long productId;
    private String productName;
    private PriceConditionType conditionType;
    private BigDecimal thresholdValue;
    private boolean active;
}
