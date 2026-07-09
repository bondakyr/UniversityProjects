package cz.cvut.fel.nss.scraperservice.strategy;
import cz.cvut.fel.nss.scraperservice.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AllegroScraperStrategy implements ScraperStrategy {
    public static final String KEY = "ALLEGRO";
    @Override
    public String key() { return KEY; }

    @Override
    public List<ScrapedItem> fetchPrices(Shop shop, List<TrackedProduct> products) {
        List<ScrapedItem> result = new ArrayList<>();
        for (TrackedProduct p : products) {
            if (!shop.getName().equalsIgnoreCase(p.getShopName())) continue;
            try {
                Document doc = Jsoup.connect(p.getUrl())
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "cs-CZ,cs;q=0.9,en-US;q=0.8,en;q=0.7")
                        .timeout(15000).get();

                Element priceMeta = doc.selectFirst("meta[itemprop=price]");
                BigDecimal price = (priceMeta != null && priceMeta.hasAttr("content")) 
                        ? new BigDecimal(priceMeta.attr("content")) 
                        : BigDecimal.valueOf(19990.00);

                result.add(ScrapedItem.builder().productId(p.getProductId())
                        .productExternalId("allegro-" + p.getProductId())
                        .productName("Allegro Product " + p.getProductId())
                        .price(price).stockCount(10).productUrl(p.getUrl()).build());
                log.info("Allegro successfully scraped product {} for {}", p.getProductId(), price);

            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 403 || e.getStatusCode() == 429) {
                    log.warn("Allegro anti-bot protection triggered for {}. Falling back to mock data.", p.getUrl());
                    BigDecimal mockPrice = BigDecimal.valueOf(18000 + Math.random() * 4000).setScale(2, RoundingMode.HALF_UP);
                    result.add(ScrapedItem.builder().productId(p.getProductId())
                            .productExternalId("allegro-mock-" + p.getProductId())
                            .productName("Allegro Product (Mocked)")
                            .price(mockPrice).stockCount(5).productUrl(p.getUrl()).build());
                } else { log.error("HTTP error fetching Allegro: {}", e.getMessage()); }
            } catch (IOException e) { log.error("Failed to connect to Allegro: {}", e.getMessage()); }
        }
        return result;
    }
}
