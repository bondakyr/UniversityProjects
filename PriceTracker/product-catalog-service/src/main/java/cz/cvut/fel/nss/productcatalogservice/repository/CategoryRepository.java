package cz.cvut.fel.nss.productcatalogservice.repository;

import cz.cvut.fel.nss.productcatalogservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}