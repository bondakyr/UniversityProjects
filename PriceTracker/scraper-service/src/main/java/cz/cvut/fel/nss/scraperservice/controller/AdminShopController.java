package cz.cvut.fel.nss.scraperservice.controller;

import cz.cvut.fel.nss.scraperservice.dto.ShopRequest;
import cz.cvut.fel.nss.scraperservice.dto.ShopResponse;
import cz.cvut.fel.nss.scraperservice.security.InternalTokenValidator;
import cz.cvut.fel.nss.scraperservice.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shops")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;
    private final InternalTokenValidator internalTokenValidator;

    @GetMapping
    public ResponseEntity<List<ShopResponse>> list(
            @RequestHeader("X-Internal-Token") String internalToken) {
        internalTokenValidator.requireValid(internalToken);
        return ResponseEntity.ok(shopService.list());
    }

    @PostMapping
    public ResponseEntity<ShopResponse> create(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestBody ShopRequest request) {
        internalTokenValidator.requireValid(internalToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(shopService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopResponse> update(@RequestHeader("X-Internal-Token") String internalToken,
                                               @PathVariable Long id,
                                               @RequestBody ShopRequest request) {
        internalTokenValidator.requireValid(internalToken);
        return ResponseEntity.ok(shopService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-Internal-Token") String internalToken,
            @PathVariable Long id) {
        internalTokenValidator.requireValid(internalToken);
        shopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
