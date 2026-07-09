package cz.cvut.fel.nss.productcatalogservice.repository;

import cz.cvut.fel.nss.productcatalogservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProductIdOrderByCreatedAtDesc(Long productId);
}
