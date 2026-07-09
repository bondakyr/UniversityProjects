package cz.cvut.fel.nss.scraperservice.config;

import cz.cvut.fel.nss.scraperservice.entity.Shop;
import cz.cvut.fel.nss.scraperservice.repository.ShopRepository;
import cz.cvut.fel.nss.scraperservice.strategy.AlzaScraperStrategy;
import cz.cvut.fel.nss.scraperservice.strategy.CzcScraperStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultShopSeeder implements CommandLineRunner {

    private final ShopRepository shopRepository;

    @Override
    public void run(String... args) {
        seed("Alza", "https://www.alza.cz", AlzaScraperStrategy.KEY);
        seed("CZC", "https://www.czc.cz", CzcScraperStrategy.KEY);
    }

    private void seed(String name, String baseUrl, String strategyKey) {
        shopRepository.findByName(name).orElseGet(() -> {
            Shop shop = new Shop();
            shop.setName(name);
            shop.setBaseUrls(baseUrl);
            shop.setStrategyKey(strategyKey);
            shop.setActive(true);
            shop.setReliabilityScore(100);
            log.info("Seeding default shop: {}", name);
            return shopRepository.save(shop);
        });
    }
}
