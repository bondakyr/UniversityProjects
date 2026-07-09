package cz.cvut.fel.nss.scraperservice.repository;

import cz.cvut.fel.nss.scraperservice.entity.ScraperLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScraperLogRepository extends JpaRepository<ScraperLog, Long> {
    List<ScraperLog> findTop50ByOrderByStartedAtDesc();
    List<ScraperLog> findByStartedAtAfter(LocalDateTime threshold);
}
