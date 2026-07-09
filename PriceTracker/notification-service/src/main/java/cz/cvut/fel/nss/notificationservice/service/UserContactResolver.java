package cz.cvut.fel.nss.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserContactResolver {

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    @Value("${services.internal.token}")
    private String internalToken;

    public Optional<String> resolveEmail(Long userId) {
        if (userId == null) return Optional.empty();
        try {
            RestClient client = RestClient.create();
            Map<?, ?> body = client.get()
                    .uri(userServiceBaseUrl + "/api/users/" + userId)
                    .header("X-Internal-Token", internalToken)
                    .retrieve()
                    .body(Map.class);
            if (body == null) return Optional.empty();
            Object email = body.get("email");
            return Optional.ofNullable(email).map(Object::toString);
        } catch (Exception ex) {
            log.warn("Could not resolve email for userId={}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }
}
