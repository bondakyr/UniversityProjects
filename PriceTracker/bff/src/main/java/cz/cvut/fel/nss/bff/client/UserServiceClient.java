package cz.cvut.fel.nss.bff.client;

import cz.cvut.fel.nss.bff.dto.LoginRequest;
import cz.cvut.fel.nss.bff.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class UserServiceClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.user.base-url}")
    private String baseUrl;

    public ResponseEntity<String> register(RegisterRequest request) {
        return post("/api/auth/register", request);
    }

    public ResponseEntity<String> login(LoginRequest request) {
        return post("/api/auth/login", request);
    }

    /**
     * Posts a JSON body and passes the downstream response through unchanged. The
     * no-op error handler stops RestClient from throwing on 4xx, so user-service
     * responses like 400 "Email already in use" or 401 bad credentials reach the
     * caller with their real status + body instead of being masked as a 500.
     */
    private ResponseEntity<String> post(String path, Object body) {
        return restClient.post()
                .uri(baseUrl + path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> { /* pass through */ })
                .toEntity(String.class);
    }
}