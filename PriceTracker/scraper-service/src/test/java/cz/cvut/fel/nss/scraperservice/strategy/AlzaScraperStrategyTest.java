package cz.cvut.fel.nss.scraperservice.strategy;

import cz.cvut.fel.nss.scraperservice.browser.PlaywrightPriceFetcher;
import cz.cvut.fel.nss.scraperservice.entity.Shop;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AlzaScraperStrategyTest {

    private final AlzaScraperStrategy strategy = new AlzaScraperStrategy(mock(PlaywrightPriceFetcher.class));

    private Shop alzaShop() {
        Shop shop = new Shop();
        shop.setName("Alza");
        shop.setStrategyKey(AlzaScraperStrategy.KEY);
        return shop;
    }

    @Test
    void key_isAlza() {
        assertThat(strategy.key()).isEqualTo("ALZA");
    }

    @Test
    void fetchPrices_skipsProductsForOtherShops() {
        List<TrackedProduct> products = List.of(new TrackedProduct(1L, "CZC", "https://czc.cz/x"));

        assertThat(strategy.fetchPrices(alzaShop(), products)).isEmpty();
    }

    @Test
    void fetchPrices_skipsProductWithNullUrl() {
        List<TrackedProduct> products = List.of(new TrackedProduct(1L, "Alza", null));

        assertThat(strategy.fetchPrices(alzaShop(), products)).isEmpty();
    }

    @Test
    void fetchPrices_returnsEmpty_forEmptyProductList() {
        assertThat(strategy.fetchPrices(alzaShop(), List.of())).isEmpty();
    }
}
