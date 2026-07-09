package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.productcatalogservice.repository.PriceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceRetentionService {

    private final PriceRecordRepository priceRecordRepository;

    @Value("${catalog.price-retention-days:1095}")
    private long retentionDays;

    @Scheduled(cron = "${catalog.price-retention-cron:0 15 3 * * *}")
    @Transactional
    public void purgeOldPriceRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        long deleted = priceRecordRepository.deleteByScrapedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Purged {} price records older than {}", deleted, cutoff);
        }
    }
}
