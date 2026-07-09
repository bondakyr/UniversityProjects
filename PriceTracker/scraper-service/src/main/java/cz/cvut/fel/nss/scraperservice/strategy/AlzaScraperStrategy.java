package cz.cvut.fel.nss.scraperservice.strategy;

import cz.cvut.fel.nss.scraperservice.browser.PlaywrightPriceFetcher;
import cz.cvut.fel.nss.scraperservice.entity.Shop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlzaScraperStrategy implements ScraperStrategy {

    public static final String KEY = "ALZA";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    private static final String[] PRICE_SELECTORS = {
            ".ads-pb__price-value", ".js-price-box__primary-price__value", ".price-box__price"
    };

    private final PlaywrightPriceFetcher priceFetcher;

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public List<ScrapedItem> fetchPrices(Shop shop, List<TrackedProduct> products) {
        List<ScrapedItem> result = new ArrayList<>();
        for (TrackedProduct p : products) {
            if (!shop.getName().equalsIgnoreCase(p.getShopName())) continue;
            if (p.getUrl() == null || p.getUrl().isBlank()) {
                log.warn("Alza: product {} has no URL, skipping.", p.getProductId());
                continue;
            }

            ScrapedItem item = scrapeWithBrowser(p);
            if (item == null) {
                item = scrapeWithHttp(p);
            }
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    private ScrapedItem scrapeWithBrowser(TrackedProduct p) {
        Optional<PlaywrightPriceFetcher.FetchedPrice> fetched = priceFetcher.fetch(p.getUrl(), PRICE_SELECTORS);
        if (fetched.isEmpty()) {
            return null;
        }
        BigDecimal price = parsePrice(fetched.get().priceText());
        if (price == null) {
            return null;
        }
        String name = fetched.get().name() != null ? fetched.get().name() : "Alza product " + p.getProductId();
        log.info("Alza (browser) scraped real price {} for product {} ({})", price, p.getProductId(), p.getUrl());
        return buildItem(p, price, name);
    }

    private ScrapedItem scrapeWithHttp(TrackedProduct p) {
        try {
            Document doc = Jsoup.connect(p.getUrl())
                    .userAgent(USER_AGENT)
                    .header("Accept-Language", "cs-CZ,cs;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Referer", "https://www.alza.cz/")
                    .timeout(15000)
                    .get();

            BigDecimal price = extractPrice(doc);
            if (price == null) {
                log.warn("Alza: could not parse a price for {}, skipping (no fake value generated).", p.getUrl());
                return null;
            }
            Element nameElement = doc.selectFirst("h1");
            String name = nameElement != null ? nameElement.text() : "Alza product " + p.getProductId();
            log.info("Alza (http) scraped real price {} for product {} ({})", price, p.getProductId(), p.getUrl());
            return buildItem(p, price, name);
        } catch (org.jsoup.HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                log.error("CRITICAL: URL does not exist (404): {}", p.getUrl());
            } else if (e.getStatusCode() == 403 || e.getStatusCode() == 429) {
                log.warn("Alza anti-bot triggered for {}", p.getUrl());
            }
        } catch (IOException e) {
            log.error("Alza fetch failed for {}: {}", p.getUrl(), e.getMessage());
        }
        return null;
    }

    private ScrapedItem buildItem(TrackedProduct p, BigDecimal price, String name) {
        return ScrapedItem.builder()
                .productId(p.getProductId())
                .productExternalId("alza-" + p.getProductId())
                .productName(name)
                .price(price)
                .stockCount(null)
                .productUrl(p.getUrl())
                .build();
    }

    private BigDecimal extractPrice(Document doc) {
        Element priceBox = doc.selectFirst(String.join(", ", PRICE_SELECTORS));
        if (priceBox != null) {
            BigDecimal parsed = parsePrice(priceBox.text());
            if (parsed != null) return parsed;
        }

        for (Element script : doc.select("script[type=application/ld+json]")) {
            BigDecimal parsed = extractPriceFromJsonLike(script.data());
            if (parsed != null) return parsed;
        }

        Element itemprop = doc.selectFirst("[itemprop=price]");
        if (itemprop != null) {
            return parsePrice(itemprop.hasAttr("content") ? itemprop.attr("content") : itemprop.text());
        }
        return null;
    }

    private BigDecimal extractPriceFromJsonLike(String raw) {
        if (raw == null) return null;
        Matcher matcher = Pattern.compile("\"price\"\\s*:\\s*\"?([0-9]+(?:[.,][0-9]+)?)\"?", Pattern.CASE_INSENSITIVE)
                .matcher(raw);
        if (matcher.find()) {
            return parsePrice(matcher.group(1));
        }
        return null;
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null) return null;
        String normalized = raw.replaceAll("[\\s\\u00a0]", "").replace(",", ".");
        Matcher matcher = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(normalized);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
