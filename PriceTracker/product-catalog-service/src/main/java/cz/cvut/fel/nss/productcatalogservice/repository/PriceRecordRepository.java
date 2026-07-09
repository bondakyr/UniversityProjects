package cz.cvut.fel.nss.productcatalogservice.repository;

import cz.cvut.fel.nss.productcatalogservice.entity.PriceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PriceRecordRepository extends JpaRepository<PriceRecord, Long> {
    List<PriceRecord> findByProductIdOrderByScrapedAtAsc(Long productId);

    Optional<PriceRecord> findTopByProductIdOrderByScrapedAtDesc(Long productId);

    long deleteByScrapedAtBefore(LocalDateTime cutoff);

    @Query("SELECT pr FROM PriceRecord pr WHERE pr.product.id = :productId " +
            "AND pr.id < (SELECT MAX(pr2.id) FROM PriceRecord pr2 WHERE pr2.product.id = :productId) " +
            "ORDER BY pr.scrapedAt DESC")
    List<PriceRecord> findPreviousByProductId(@Param("productId") Long productId);
}
