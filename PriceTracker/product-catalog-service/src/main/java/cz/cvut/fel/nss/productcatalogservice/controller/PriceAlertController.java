package cz.cvut.fel.nss.productcatalogservice.controller;

import cz.cvut.fel.nss.productcatalogservice.dto.alert.PriceAlertRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.alert.PriceAlertResponse;
import cz.cvut.fel.nss.productcatalogservice.security.InternalTokenValidator;
import cz.cvut.fel.nss.productcatalogservice.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class PriceAlertController {

    private final PriceAlertService priceAlertService;
    private final InternalTokenValidator internalTokenValidator;

    @PostMapping
    public ResponseEntity<PriceAlertResponse> create(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PriceAlertRequest request) {
        internalTokenValidator.requireValid(internalToken);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(priceAlertService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<PriceAlertResponse>> list(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestHeader("X-User-Id") Long userId) {
        internalTokenValidator.requireValid(internalToken);
        return ResponseEntity.ok(priceAlertService.listForUser(userId));
    }

    @PostMapping("/{alertId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long alertId) {
        internalTokenValidator.requireValid(internalToken);
        priceAlertService.deactivate(userId, alertId);
        return ResponseEntity.noContent().build();
    }
}
