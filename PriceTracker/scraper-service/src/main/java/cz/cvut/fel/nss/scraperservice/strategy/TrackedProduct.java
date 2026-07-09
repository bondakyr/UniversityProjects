package cz.cvut.fel.nss.scraperservice.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackedProduct {
    private Long productId;
    private String shopName;
    private String url;
}
