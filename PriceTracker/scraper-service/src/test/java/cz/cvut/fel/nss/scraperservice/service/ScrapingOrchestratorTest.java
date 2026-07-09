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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScrapingOrchestratorTest {

    @Mock
    private ShopRepository shopRepository;
    @Mock
    private ScraperLogRepository scraperLogRepository;
    @Mock
    private ScraperStrategyFactory strategyFactory;
    @Mock
    private PriceUpdatePublisher publisher;
    @Mock
    private TrackedProductsProperties trackedProductsProperties;
    @Mock
    private CatalogScrapeClient catalogScrapeClient;

    @InjectMocks
    private ScrapingOrchestrator orchestrator;

    private Shop shop;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setId(1L);
        shop.setName("Alza");
        shop.setStrategyKey("ALZA");
        shop.setActive(true);
        lenient().when(scraperLogRepository.save(any(ScraperLog.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ScrapedItem item(Long productId) {
        return ScrapedItem.builder()
                .productId(productId)
                .price(new BigDecimal("1000"))
                .productUrl("https://x")
                .build();
    }

    @Test
    void scrapeShop_publishesEvents_andRecordsOkLog() throws Exception {
        ScraperStrategy strategy = mock(ScraperStrategy.class);
        when(strategy.fetchPrices(eq(shop), any())).thenReturn(List.of(item(1L), item(2L)));
        when(strategyFactory.get("ALZA")).thenReturn(Optional.of(strategy));

        ScraperLog log = orchestrator.scrapeShop(shop, List.of());

        verify(publisher, times(2)).publish(any(PriceUpdateEvent.class));
        assertThat(log.getStatus()).isEqualTo("OK");
        assertThat(log.getSuccessCount()).isEqualTo(2);
        assertThat(log.getFinishedAt()).isNotNull();
    }

    @Test
    void scrapeShop_recordsMissingStrategy_whenStrategyAbsent() {
        when(strategyFactory.get("ALZA")).thenReturn(Optional.empty());

        ScraperLog log = orchestrator.scrapeShop(shop, List.of());

        assertThat(log.getStatus()).isEqualTo("MISSING_STRATEGY");
        verifyNoInteractions(publisher);
    }

    @Test
    void scrapeShop_classifiesBlocked_whenStrategyReports403() throws Exception {
        ScraperStrategy strategy = mock(ScraperStrategy.class);
        when(strategy.fetchPrices(eq(shop), any())).thenThrow(new RuntimeException("HTTP 403 blocked by captcha"));
        when(strategyFactory.get("ALZA")).thenReturn(Optional.of(strategy));

        ScraperLog log = orchestrator.scrapeShop(shop, List.of());

        assertThat(log.getStatus()).isEqualTo("BLOCKED");
        assertThat(log.getBlockedCount()).isEqualTo(1);
        verifyNoInteractions(publisher);
    }

    @Test
    void scrapeShop_classifiesFailed_onGenericError() throws Exception {
        ScraperStrategy strategy = mock(ScraperStrategy.class);
        when(strategy.fetchPrices(eq(shop), any())).thenThrow(new RuntimeException("connection reset"));
        when(strategyFactory.get("ALZA")).thenReturn(Optional.of(strategy));

        ScraperLog log = orchestrator.scrapeShop(shop, List.of());

        assertThat(log.getStatus()).isEqualTo("FAILED");
        assertThat(log.getFailureCount()).isEqualTo(1);
    }

    @Test
    void runForAllActiveShops_isolatesFailures_andProcessesEveryShop() throws Exception {
        Shop other = new Shop();
        other.setId(2L);
        other.setName("CZC");
        other.setStrategyKey("CZC");
        other.setActive(true);

        when(shopRepository.findByActiveTrue()).thenReturn(List.of(shop, other));
        when(catalogScrapeClient.fetchTargets()).thenReturn(List.of());
        when(trackedProductsProperties.asTrackedProducts())
                .thenReturn(List.of(new TrackedProduct(1L, "Alza", "u")));

        ScraperStrategy alza = mock(ScraperStrategy.class);
        when(alza.fetchPrices(eq(shop), any())).thenThrow(new RuntimeException("boom"));
        ScraperStrategy czc = mock(ScraperStrategy.class);
        when(czc.fetchPrices(eq(other), any())).thenReturn(List.of(item(9L)));

        when(strategyFactory.get("ALZA")).thenReturn(Optional.of(alza));
        when(strategyFactory.get("CZC")).thenReturn(Optional.of(czc));

        orchestrator.runForAllActiveShops();

        verify(czc).fetchPrices(eq(other), any());
        verify(publisher, times(1)).publish(any(PriceUpdateEvent.class));
    }

    @Test
    void runForAllActiveShops_doesNothing_whenNoActiveShops() {
        when(shopRepository.findByActiveTrue()).thenReturn(List.of());

        orchestrator.runForAllActiveShops();

        verifyNoInteractions(publisher);
        verify(scraperLogRepository, never()).save(any());
    }
}
