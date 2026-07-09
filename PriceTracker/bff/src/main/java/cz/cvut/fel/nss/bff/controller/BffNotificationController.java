package cz.cvut.fel.nss.bff.controller;

import cz.cvut.fel.nss.bff.client.NotificationServiceClient;
import cz.cvut.fel.nss.bff.security.AuthenticatedUser;
import cz.cvut.fel.nss.bff.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class BffNotificationController {

    private final NotificationServiceClient notificationServiceClient;
    private final AuthenticatedUserResolver userResolver;

    @GetMapping
    public ResponseEntity<String> myNotifications(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        AuthenticatedUser user = userResolver.resolve(auth)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        return notificationServiceClient.myNotifications(user);
    }
}
