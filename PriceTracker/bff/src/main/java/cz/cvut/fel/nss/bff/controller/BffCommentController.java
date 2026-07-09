package cz.cvut.fel.nss.bff.controller;

import cz.cvut.fel.nss.bff.client.CatalogServiceClient;
import cz.cvut.fel.nss.bff.security.AuthenticatedUser;
import cz.cvut.fel.nss.bff.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class BffCommentController {

    private final CatalogServiceClient catalogServiceClient;
    private final AuthenticatedUserResolver userResolver;

    @PostMapping
    public ResponseEntity<String> add(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body) {
        AuthenticatedUser user = userResolver.resolve(auth)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        return catalogServiceClient.addComment(user, body);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<String> list(@PathVariable Long productId) {
        return catalogServiceClient.getCommentsForProduct(productId);
    }
}
