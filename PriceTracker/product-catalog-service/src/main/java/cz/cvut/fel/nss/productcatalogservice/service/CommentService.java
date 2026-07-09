package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.dto.comment.CommentRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.comment.CommentResponse;
import cz.cvut.fel.nss.productcatalogservice.entity.Comment;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.repository.CommentRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CommentResponse add(Long userId, String userLogin, CommentRequest request) {
        if (request.getText() == null || request.getText().isBlank()) {
            throw new BadRequestException("Comment text is required");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Comment comment = new Comment();
        comment.setProduct(product);
        comment.setText(request.getText().trim());
        comment.setUserId(userId);
        comment.setUserLogin(userLogin);
        return toResponse(commentRepository.save(comment));
    }

    public List<CommentResponse> listForProduct(Long productId) {
        return commentRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .userLogin(c.getUserLogin())
                .text(c.getText())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
