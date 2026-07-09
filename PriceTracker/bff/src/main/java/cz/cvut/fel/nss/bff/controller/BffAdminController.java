package cz.cvut.fel.nss.bff.controller;

import cz.cvut.fel.nss.bff.client.CatalogServiceClient;
import cz.cvut.fel.nss.bff.client.ScraperServiceClient;
import cz.cvut.fel.nss.bff.security.AuthenticatedUser;
import cz.cvut.fel.nss.bff.security.AuthenticatedUserResolver;
import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class BffAdminController {

    private final ScraperServiceClient scraperServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final AuthenticatedUserResolver userResolver;

    @GetMapping("/scraper/dashboard")
    public ResponseEntity<String> dashboard(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        requireAdmin(auth);
        return scraperServiceClient.dashboard();
    }

    @PostMapping("/scraper/run")
    public ResponseEntity<String> triggerRun(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        requireAdmin(auth);
        return scraperServiceClient.triggerRun();
    }

    @PostMapping("/products")
    public ResponseEntity<String> createProduct(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body) {
        AuthenticatedUser admin = requireAdmin(auth);
        return catalogServiceClient.createProduct(admin, body);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<String> updateProduct(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable Long id,
            @RequestBody String body) {
        AuthenticatedUser admin = requireAdmin(auth);
        return catalogServiceClient.updateProduct(admin, id, body);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable Long id) {
        AuthenticatedUser admin = requireAdmin(auth);
        return catalogServiceClient.deleteProduct(admin, id);
    }

    @GetMapping("/shops")
    public ResponseEntity<String> listShops(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        requireAdmin(auth);
        return scraperServiceClient.listShops();
    }

    @PostMapping("/shops")
    public ResponseEntity<String> createShop(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body) {
        requireAdmin(auth);
        return scraperServiceClient.createShop(body);
    }

    @PutMapping("/shops/{id}")
    public ResponseEntity<String> updateShop(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable Long id,
            @RequestBody String body) {
        requireAdmin(auth);
        return scraperServiceClient.updateShop(id, body);
    }

    @DeleteMapping("/shops/{id}")
    public ResponseEntity<String> deleteShop(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable Long id) {
        requireAdmin(auth);
        return scraperServiceClient.deleteShop(id);
    }

    private AuthenticatedUser requireAdmin(String authHeader) {
        AuthenticatedUser user = userResolver.resolve(authHeader)
                .orElseThrow(() -> new BadRequestException("Unauthenticated"));
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Admin role required");
        }
        return user;
    }
}