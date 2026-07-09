package cz.cvut.fel.nss.bff.security;

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
public class AuthenticatedUserResolver {

    private final BffJwtParser bffJwtParser;

    @Value("${services.user.base-url}")
    private String userBaseUrl;

    @Value("${services.internal.token}")
    private String internalToken;

    public Optional<AuthenticatedUser> resolve(String authHeader) {
        return bffJwtParser.extractEmail(authHeader).flatMap(this::loadByEmail);
    }

    private Optional<AuthenticatedUser> loadByEmail(String email) {
        try {
            RestClient client = RestClient.create();
            Map<?, ?> body = client.get()
                    .uri(userBaseUrl + "/api/users/by-email/" + email)
                    .header("X-Internal-Token", internalToken)
                    .retrieve()
                    .body(Map.class);
            if (body == null) return Optional.empty();
            Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
            String login = body.get("login") == null ? null : body.get("login").toString();
            String role = body.get("role") == null ? null : body.get("role").toString();
            return Optional.of(new AuthenticatedUser(id, email, login, role));
        } catch (Exception ex) {
            log.warn("Failed to resolve user by email {}: {}", email, ex.getMessage());
            return Optional.empty();
        }
    }
}
