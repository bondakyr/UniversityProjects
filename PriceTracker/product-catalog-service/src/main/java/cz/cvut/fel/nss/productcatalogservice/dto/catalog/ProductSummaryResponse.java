package cz.cvut.fel.nss.productcatalogservice.dto.catalog;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductSummaryResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String categoryName;
    private BigDecimal currentPrice;
    private String shopName;
}
