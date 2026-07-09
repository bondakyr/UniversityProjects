package cz.cvut.fel.nss.scraperservice.client;

import cz.cvut.fel.nss.scraperservice.strategy.TrackedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class CatalogScrapeClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.catalog.base-url}")
    private String catalogBaseUrl;

    public List<TrackedProduct> fetchTargets() {
        try {
            ScrapeTarget[] targets = restClient.get()
                    .uri(catalogBaseUrl + "/api/internal/scrape-targets")
                    .retrieve()
                    .body(ScrapeTarget[].class);
            if (targets == null) {
                return List.of();
            }
            return Arrays.stream(targets)
                    .filter(t -> t.productId() != null && t.productUrl() != null && !t.productUrl().isBlank())
                    .map(t -> new TrackedProduct(t.productId(), t.shopName(), t.productUrl()))
                    .toList();
        } catch (Exception ex) {
            log.warn("Could not fetch scrape targets from catalog: {}", ex.getMessage());
            return List.of();
        }
    }

    public record ScrapeTarget(Long productId, String productUrl, String shopName) {
    }
}
