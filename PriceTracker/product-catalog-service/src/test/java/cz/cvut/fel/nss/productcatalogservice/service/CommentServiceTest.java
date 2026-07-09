package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.dto.comment.CommentRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.comment.CommentResponse;
import cz.cvut.fel.nss.productcatalogservice.entity.Comment;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.repository.CommentRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CommentService service;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
    }

    @Test
    void add_savesComment_andTrimsText() {
        CommentRequest req = new CommentRequest();
        req.setProductId(1L);
        req.setText("  great price drop  ");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentResponse resp = service.add(3L, "alice", req);

        assertThat(resp.getText()).isEqualTo("great price drop");
        assertThat(resp.getUserId()).isEqualTo(3L);
        assertThat(resp.getUserLogin()).isEqualTo("alice");
    }

    @Test
    void add_throws_whenTextBlank() {
        CommentRequest req = new CommentRequest();
        req.setProductId(1L);
        req.setText("   ");

        assertThatThrownBy(() -> service.add(3L, "alice", req))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    void add_throws_whenTextNull() {
        CommentRequest req = new CommentRequest();
        req.setProductId(1L);

        assertThatThrownBy(() -> service.add(3L, "alice", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void add_throws_whenProductNotFound() {
        CommentRequest req = new CommentRequest();
        req.setProductId(99L);
        req.setText("hi");
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(3L, "alice", req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listForProduct_mapsAllComments() {
        Comment c = new Comment();
        c.setId(1L);
        c.setUserId(3L);
        c.setText("hi");
        c.setProduct(product);
        when(commentRepository.findByProductIdOrderByCreatedAtDesc(1L)).thenReturn(java.util.List.of(c));

        var result = service.listForProduct(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo("hi");
    }
}
