package cz.cvut.fel.nss.notificationservice.controller;

import cz.cvut.fel.nss.notificationservice.dto.NotificationLogResponse;
import cz.cvut.fel.nss.notificationservice.service.PriceDropNotificationService;
import cz.cvut.fel.nss.notificationservice.security.InternalTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final PriceDropNotificationService priceDropNotificationService;
    private final InternalTokenValidator internalTokenValidator;

    @GetMapping
    public ResponseEntity<List<NotificationLogResponse>> myNotifications(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestHeader("X-User-Id") Long userId) {
        internalTokenValidator.requireValid(internalToken);
        return ResponseEntity.ok(priceDropNotificationService.listForUser(userId));
    }
}
