package cz.cvut.fel.nss.bff.client;

import cz.cvut.fel.nss.bff.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationServiceClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.notification.base-url}")
    private String baseUrl;

    @Value("${services.internal.token}")
    private String internalToken;

    public ResponseEntity<String> myNotifications(AuthenticatedUser user) {
        return restClient.get()
                .uri(baseUrl + "/api/notifications")
                .header("X-User-Id", String.valueOf(user.getId()))
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toEntity(String.class);
    }
}
