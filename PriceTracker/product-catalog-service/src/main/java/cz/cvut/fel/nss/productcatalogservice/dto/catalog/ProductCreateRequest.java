package cz.cvut.fel.nss.productcatalogservice.dto.catalog;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private String imageUrl;
    private String categoryName;

    private BigDecimal initialPrice;
    private String shopName;
    private String productUrl;
}