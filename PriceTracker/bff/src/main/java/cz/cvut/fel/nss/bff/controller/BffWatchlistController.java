package cz.cvut.fel.nss.bff.controller;

import cz.cvut.fel.nss.bff.client.CatalogServiceClient;
import cz.cvut.fel.nss.bff.security.AuthenticatedUser;
import cz.cvut.fel.nss.bff.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class BffWatchlistController {

    private final CatalogServiceClient catalogServiceClient;
    private final AuthenticatedUserResolver userResolver;

    @GetMapping
    public ResponseEntity<String> list(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        AuthenticatedUser user = userResolver.resolve(auth)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        return catalogServiceClient.watchlistList(user);
    }

    @PostMapping
    public ResponseEntity<String> add(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body) {
        AuthenticatedUser user = userResolver.resolve(auth)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        return catalogServiceClient.watchlistAdd(user, body);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> remove(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable Long itemId) {
        AuthenticatedUser user = userResolver.resolve(auth)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        catalogServiceClient.watchlistRemove(user, itemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
