package cz.cvut.fel.nss.scraperservice.repository;

import cz.cvut.fel.nss.scraperservice.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByActiveTrue();
    Optional<Shop> findByName(String name);
}
