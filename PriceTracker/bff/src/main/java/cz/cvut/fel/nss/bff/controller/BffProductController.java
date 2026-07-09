package cz.cvut.fel.nss.bff.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.nss.bff.client.CatalogServiceClient;
import cz.cvut.fel.nss.bff.dto.AggregatedProductDetail;
import cz.cvut.fel.nss.bff.security.AuthenticatedUser;
import cz.cvut.fel.nss.bff.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class BffProductController {

    private final CatalogServiceClient catalogServiceClient;
    private final AuthenticatedUserResolver userResolver;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<String> listProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String shopName,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, String> q = new LinkedHashMap<>();
        if (name != null) q.put("name", name);
        if (categoryId != null) q.put("categoryId", String.valueOf(categoryId));
        if (shopName != null) q.put("shopName", shopName);
        if (minPrice != null) q.put("minPrice", minPrice);
        if (maxPrice != null) q.put("maxPrice", maxPrice);
        q.put("page", String.valueOf(page));
        q.put("size", String.valueOf(size));
        return catalogServiceClient.getProducts(q);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AggregatedProductDetail> getProductDetail(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        JsonNode product = parse(catalogServiceClient.getProductDetail(id).getBody());
        JsonNode comments = parse(catalogServiceClient.getCommentsForProduct(id).getBody());

        Boolean inWatchlist = null;
        AuthenticatedUser user = userResolver.resolve(authHeader).orElse(null);
        if (user != null) {
            JsonNode list = parse(catalogServiceClient.watchlistList(user).getBody());
            if (list != null && list.isArray()) {
                inWatchlist = false;
                for (JsonNode item : list) {
                    if (item.path("productId").asLong(-1) == id) {
                        inWatchlist = true;
                        break;
                    }
                }
            }
        }

        AggregatedProductDetail aggregated = AggregatedProductDetail.builder()
                .product(product)
                .comments(comments)
                .inWatchlist(inWatchlist)
                .build();

        return ResponseEntity.ok(aggregated);
    }

    @GetMapping(value = "/{id}/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportPriceHistory(@PathVariable Long id) {
        ResponseEntity<byte[]> upstream = catalogServiceClient.exportPriceHistory(id);
        return ResponseEntity.status(upstream.getStatusCode())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"price-history-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(upstream.getBody());
    }

    private JsonNode parse(String body) {
        if (body == null) return null;
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            log.warn("Failed to parse JSON from upstream: {}", e.getMessage());
            return null;
        }
    }
}
