package cz.cvut.fel.nss.scraperservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopResponse {
    private Long id;
    private String name;
    private String baseUrls;
    private String strategyKey;
    private boolean active;
    private Integer reliabilityScore;
}
