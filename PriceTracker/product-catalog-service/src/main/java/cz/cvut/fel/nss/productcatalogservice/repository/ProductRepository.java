package cz.cvut.fel.nss.productcatalogservice.repository;

import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.priceRecords pr " +
            "WHERE (CAST(:name AS string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) " +
            "AND (:catId IS NULL OR p.category.id = :catId) " +
            "AND (CAST(:shopName AS string) IS NULL OR LOWER(pr.shopName) = LOWER(CAST(:shopName AS string))) " +
            "AND (:minPrice IS NULL OR pr.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR pr.price <= :maxPrice)")
    Page<Product> search(@Param("name") String name,
                         @Param("catId") Long catId,
                         @Param("shopName") String shopName,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         Pageable pageable);
}