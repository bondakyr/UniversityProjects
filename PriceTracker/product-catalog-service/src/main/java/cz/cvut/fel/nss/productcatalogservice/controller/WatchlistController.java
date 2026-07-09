package cz.cvut.fel.nss.productcatalogservice.controller;

import cz.cvut.fel.nss.productcatalogservice.dto.watchlist.WatchlistItemRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.watchlist.WatchlistItemResponse;
import cz.cvut.fel.nss.productcatalogservice.security.InternalTokenValidator;
import cz.cvut.fel.nss.productcatalogservice.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final InternalTokenValidator internalTokenValidator;

    @PostMapping
    public ResponseEntity<WatchlistItemResponse> add(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody WatchlistItemRequest request) {
        internalTokenValidator.requireValid(internalToken);
        WatchlistItemResponse response = watchlistService.add(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WatchlistItemResponse>> list(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestHeader("X-User-Id") Long userId) {
        internalTokenValidator.requireValid(internalToken);
        return ResponseEntity.ok(watchlistService.list(userId));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> remove(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        internalTokenValidator.requireValid(internalToken);
        watchlistService.remove(userId, itemId);
        return ResponseEntity.noContent().build();
    }
}
