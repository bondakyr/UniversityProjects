package cz.cvut.fel.nss.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ScraperServiceClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.scraper.base-url}")
    private String baseUrl;

    @Value("${services.internal.token}")
    private String internalToken;

    public ResponseEntity<String> dashboard() {
        return restClient.get()
                .uri(baseUrl + "/api/admin/scraper/dashboard")
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> listShops() {
        return restClient.get()
                .uri(baseUrl + "/api/admin/shops")
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> createShop(String json) {
        return restClient.post()
                .uri(baseUrl + "/api/admin/shops")
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> updateShop(Long id, String json) {
        return restClient.put()
                .uri(baseUrl + "/api/admin/shops/" + id)
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> deleteShop(Long id) {
        return restClient.delete()
                .uri(baseUrl + "/api/admin/shops/" + id)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> triggerRun() {
        return restClient.post()
                .uri(baseUrl + "/api/admin/scraper/run")
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }
}