package cz.cvut.fel.nss.scraperservice.dto;

import lombok.Data;

@Data
public class ShopRequest {
    private String name;
    private String baseUrls;
    private String strategyKey;
    private Boolean active;
    private Integer reliabilityScore;
}
