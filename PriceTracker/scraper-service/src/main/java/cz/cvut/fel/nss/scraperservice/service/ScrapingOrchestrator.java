package cz.cvut.fel.nss.scraperservice.service;

import cz.cvut.fel.nss.scraperservice.client.CatalogScrapeClient;
import cz.cvut.fel.nss.scraperservice.config.TrackedProductsProperties;
import cz.cvut.fel.nss.scraperservice.entity.ScraperLog;
import cz.cvut.fel.nss.scraperservice.entity.Shop;
import cz.cvut.fel.nss.scraperservice.kafka.PriceUpdateEvent;
import cz.cvut.fel.nss.scraperservice.kafka.PriceUpdatePublisher;
import cz.cvut.fel.nss.scraperservice.repository.ScraperLogRepository;
import cz.cvut.fel.nss.scraperservice.repository.ShopRepository;
import cz.cvut.fel.nss.scraperservice.strategy.ScrapedItem;
import cz.cvut.fel.nss.scraperservice.strategy.ScraperStrategy;
import cz.cvut.fel.nss.scraperservice.strategy.ScraperStrategyFactory;
import cz.cvut.fel.nss.scraperservice.strategy.TrackedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingOrchestrator {

    private final ShopRepository shopRepository;
    private final ScraperLogRepository scraperLogRepository;
    private final ScraperStrategyFactory strategyFactory;
    private final PriceUpdatePublisher publisher;
    private final TrackedProductsProperties trackedProductsProperties;
    private final CatalogScrapeClient catalogScrapeClient;

    @Scheduled(
            initialDelayString = "${scraper.initial-delay-ms}",
            fixedDelayString = "${scraper.fixed-delay-ms}"
    )
    public void runDailyScrape() {
        log.info("=== Starting scheduled scrape run ===");
        runForAllActiveShops();
    }

    public void runForAllActiveShops() {
        List<Shop> shops = shopRepository.findByActiveTrue();
        if (shops.isEmpty()) {
            log.info("No active shops configured, skipping scrape run.");
            return;
        }
        List<TrackedProduct> tracked = catalogScrapeClient.fetchTargets();
        if (tracked.isEmpty()) {
            tracked = trackedProductsProperties.asTrackedProducts();
        }

        for (Shop shop : shops) {
            try {
                scrapeShop(shop, tracked);
            } catch (Exception ex) {
                log.error("Shop {} scrape failed (isolated): {}", shop.getName(), ex.getMessage(), ex);
            }
        }
    }

    public ScraperLog scrapeShop(Shop shop, List<TrackedProduct> tracked) {
        ScraperLog logRecord = new ScraperLog();
        logRecord.setShop(shop);
        logRecord.setStartedAt(LocalDateTime.now());
        logRecord.setStatus("RUNNING");

        ScraperStrategy strategy = strategyFactory.get(shop.getStrategyKey()).orElse(null);
        if (strategy == null) {
            logRecord.setFinishedAt(LocalDateTime.now());
            logRecord.setStatus("MISSING_STRATEGY");
            logRecord.setErrorMessage("No strategy registered for key=" + shop.getStrategyKey());
            return scraperLogRepository.save(logRecord);
        }

        try {
            List<ScrapedItem> items = strategy.fetchPrices(shop, tracked);
            int success = 0;
            int failure = 0;
            for (ScrapedItem item : items) {
                try {
                    publisher.publish(PriceUpdateEvent.builder()
                            .productId(item.getProductId())
                            .productExternalId(item.getProductExternalId())
                            .productName(item.getProductName())
                            .price(item.getPrice())
                            .stockCount(item.getStockCount())
                            .productUrl(item.getProductUrl())
                            .shopName(shop.getName())
                            .build());
                    success++;
                } catch (Exception perItem) {
                    failure++;
                    log.warn("Failed to publish item for product {}: {}",
                            item.getProductId(), perItem.getMessage());
                }
            }
            logRecord.setSuccessCount(success);
            logRecord.setFailureCount(failure);
            logRecord.setStatus(failure == 0 ? "OK" : "PARTIAL");
        } catch (Exception ex) {
            logRecord.setStatus(classifyError(ex));
            logRecord.setErrorMessage(ex.getMessage());
            if ("BLOCKED".equals(logRecord.getStatus())) {
                logRecord.setBlockedCount(1);
            } else {
                logRecord.setFailureCount(1);
            }
            log.warn("Shop {} scrape error: {}", shop.getName(), ex.getMessage());
        } finally {
            logRecord.setFinishedAt(LocalDateTime.now());
            scraperLogRepository.save(logRecord);
        }
        return logRecord;
    }

    private String classifyError(Exception ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (msg.contains("403") || msg.contains("captcha") || msg.contains("blocked")) {
            return "BLOCKED";
        }
        return "FAILED";
    }
}
