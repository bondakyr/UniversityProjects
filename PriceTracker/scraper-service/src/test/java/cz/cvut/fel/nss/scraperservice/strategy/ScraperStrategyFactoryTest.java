package cz.cvut.fel.nss.scraperservice.strategy;

import cz.cvut.fel.nss.scraperservice.browser.PlaywrightPriceFetcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ScraperStrategyFactoryTest {

    private final ScraperStrategyFactory factory =
            new ScraperStrategyFactory(List.of(
                    new AlzaScraperStrategy(mock(PlaywrightPriceFetcher.class)), new CzcScraperStrategy()));

    @Test
    void get_resolvesStrategy_caseInsensitively() {
        assertThat(factory.get("alza")).containsInstanceOf(AlzaScraperStrategy.class);
        assertThat(factory.get("ALZA")).containsInstanceOf(AlzaScraperStrategy.class);
        assertThat(factory.get("czc")).containsInstanceOf(CzcScraperStrategy.class);
    }

    @Test
    void get_returnsEmpty_forUnknownKey() {
        assertThat(factory.get("ebay")).isEmpty();
    }

    @Test
    void get_returnsEmpty_forNullKey() {
        assertThat(factory.get(null)).isEmpty();
    }

    @Test
    void registeredKeys_listsAllStrategiesSorted() {
        assertThat(factory.registeredKeys()).containsExactly("ALZA", "CZC");
    }
}
