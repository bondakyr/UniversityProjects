package cz.cvut.fel.nss.bff.client;

import cz.cvut.fel.nss.bff.security.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
public class CatalogServiceClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.catalog.base-url}")
    private String baseUrl;

    @Value("${services.internal.token}")
    private String internalToken;

    public ResponseEntity<String> getProducts(Map<String, String> queryParams) {
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(baseUrl + "/api/products");
        queryParams.forEach(b::queryParam);
        URI uri = b.build().toUri();
        return restClient.get().uri(uri).retrieve().toEntity(String.class);
    }

    public ResponseEntity<String> createProduct(AuthenticatedUser user, String json) {
        return restClient.post()
                .uri(baseUrl + "/api/products")
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> getProductDetail(Long id) {
        return restClient.get()
                .uri(baseUrl + "/api/products/" + id)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> updateProduct(AuthenticatedUser user, Long id, String json) {
        return restClient.put()
                .uri(baseUrl + "/api/products/" + id)
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> deleteProduct(AuthenticatedUser user, Long id) {
        return restClient.delete()
                .uri(baseUrl + "/api/products/" + id)
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<byte[]> exportPriceHistory(Long id) {
        return restClient.get()
                .uri(baseUrl + "/api/products/" + id + "/export")
                .retrieve()
                .toEntity(byte[].class);
    }

    public ResponseEntity<String> getCommentsForProduct(Long productId) {
        return restClient.get()
                .uri(baseUrl + "/api/comments/product/" + productId)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> watchlistList(AuthenticatedUser user) {
        return restClient.get()
                .uri(baseUrl + "/api/watchlist")
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> watchlistAdd(AuthenticatedUser user, String json) {
        return restClient.post()
                .uri(baseUrl + "/api/watchlist")
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> watchlistRemove(AuthenticatedUser user, Long itemId) {
        return restClient.delete()
                .uri(baseUrl + "/api/watchlist/" + itemId)
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> addComment(AuthenticatedUser user, String json) {
        return restClient.post()
                .uri(baseUrl + "/api/comments")
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> alertList(AuthenticatedUser user) {
        return restClient.get()
                .uri(baseUrl + "/api/alerts")
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> alertCreate(AuthenticatedUser user, String json) {
        return restClient.post()
                .uri(baseUrl + "/api/alerts")
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> alertDeactivate(AuthenticatedUser user, Long alertId) {
        return restClient.post()
                .uri(baseUrl + "/api/alerts/" + alertId + "/deactivate")
                .headers(forwardIdentity(user))
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }

    private Consumer<org.springframework.http.HttpHeaders> forwardIdentity(AuthenticatedUser user) {
        return headers -> {
            headers.set("X-User-Id", String.valueOf(user.getId()));
            if (user.getLogin() != null) headers.set("X-User-Login", user.getLogin());
            if (user.getRole() != null) headers.set("X-User-Role", user.getRole());
        };
    }
}