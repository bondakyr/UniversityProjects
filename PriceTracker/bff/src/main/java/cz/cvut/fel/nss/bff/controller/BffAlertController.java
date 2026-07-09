package cz.cvut.fel.nss.bff.controller;

import cz.cvut.fel.nss.bff.client.CatalogServiceClient;
import cz.cvut.fel.nss.bff.security.AuthenticatedUser;
import cz.cvut.fel.nss.bff.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class BffAlertController {

    private final CatalogServiceClient catalogServiceClient;
    private final AuthenticatedUserResolver userResolver;

    @GetMapping
    public ResponseEntity<String> list(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        AuthenticatedUser user = userResolver.resolve(auth)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        return catalogServiceClient.alertList(user);
    }

    @PostMapping
    public ResponseEntity<String> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body) {
        AuthenticatedUser user = userResolver.resolve(auth)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        return catalogServiceClient.alertCreate(user, body);
    }

    @PostMapping("/{alertId}/deactivate")
    public ResponseEntity<String> deactivate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable Long alertId) {
        AuthenticatedUser user = userResolver.resolve(auth)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        return catalogServiceClient.alertDeactivate(user, alertId);
    }
}