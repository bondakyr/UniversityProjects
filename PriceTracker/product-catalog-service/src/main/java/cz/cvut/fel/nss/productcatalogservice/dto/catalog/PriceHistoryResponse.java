package cz.cvut.fel.nss.productcatalogservice.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PriceHistoryResponse implements java.io.Serializable {
    private BigDecimal price;
    private String shopName;
    private LocalDateTime date;
}