package cz.cvut.fel.nss.productcatalogservice.controller;

import cz.cvut.fel.nss.productcatalogservice.dto.comment.CommentRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.comment.CommentResponse;
import cz.cvut.fel.nss.productcatalogservice.security.InternalTokenValidator;
import cz.cvut.fel.nss.productcatalogservice.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final InternalTokenValidator internalTokenValidator;

    @PostMapping
    public ResponseEntity<CommentResponse> add(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Login", required = false) String userLogin,
            @RequestBody CommentRequest request) {
        internalTokenValidator.requireValid(internalToken);
        CommentResponse response = commentService.add(userId, userLogin, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CommentResponse>> list(@PathVariable Long productId) {
        return ResponseEntity.ok(commentService.listForProduct(productId));
    }
}
