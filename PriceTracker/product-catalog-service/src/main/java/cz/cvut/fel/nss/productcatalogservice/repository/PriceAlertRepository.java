package cz.cvut.fel.nss.productcatalogservice.repository;

import cz.cvut.fel.nss.productcatalogservice.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByProductIdAndActiveTrue(Long productId);

    List<PriceAlert> findByUserId(Long userId);
}
