package cz.cvut.fel.nss.scraperservice.strategy;

import cz.cvut.fel.nss.scraperservice.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CzcScraperStrategy implements ScraperStrategy {

    public static final String KEY = "CZC";

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public List<ScrapedItem> fetchPrices(Shop shop, List<TrackedProduct> products) {
        List<ScrapedItem> result = new ArrayList<>();
        for (TrackedProduct p : products) {
            if (!shop.getName().equalsIgnoreCase(p.getShopName())) continue;

            try {
                Document doc = Jsoup.connect(p.getUrl())
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(10000)
                        .get();

                Element priceElement = doc.selectFirst(".price-v-text");
                if (priceElement == null) {
                    priceElement = doc.selectFirst(".price");
                }
                
                if (priceElement == null) {
                    log.warn("Could not find price element for CZC product: {}", p.getUrl());
                    continue;
                }

                String rawPrice = priceElement.text().replaceAll("[^0-9]", "");
                BigDecimal price = new BigDecimal(rawPrice);

                Element nameElement = doc.selectFirst("h1");
                String productName = nameElement != null ? nameElement.text() : "CZC product " + p.getProductId();

                result.add(ScrapedItem.builder()
                        .productId(p.getProductId())
                        .productExternalId("czc-" + p.getProductId())
                        .productName(productName)
                        .price(price)
                        .stockCount(5)
                        .productUrl(p.getUrl())
                        .build());

                log.info("CZC successfully scraped product {} for {}", p.getProductId(), price);

            } catch (IOException e) {
                log.error("Failed to scrape CZC product at {}: {}", p.getUrl(), e.getMessage());
            } catch (NumberFormatException e) {
                log.error("Failed to parse price for CZC product at {}", p.getUrl());
            }
        }
        return result;
    }
}
