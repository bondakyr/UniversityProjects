package cz.cvut.fel.nss.productcatalogservice.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ScrapeTargetResponse {
    private Long productId;
    private String productUrl;
    private String shopName;
}
