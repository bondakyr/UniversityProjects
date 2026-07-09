package cz.cvut.fel.nss.productcatalogservice.repository;

import cz.cvut.fel.nss.productcatalogservice.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUserId(Long userId);

    List<WatchlistItem> findByProductId(Long productId);

    Optional<WatchlistItem> findByUserIdAndProductId(Long userId, Long productId);
}
