package cz.cvut.fel.nss.productcatalogservice.dto.alert;

import cz.cvut.fel.nss.productcatalogservice.entity.PriceConditionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceAlertRequest {
    private Long productId;
    private PriceConditionType conditionType;
    private BigDecimal thresholdValue;
}
