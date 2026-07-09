package cz.cvut.fel.nss.scraperservice.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PlaywrightPriceFetcher {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private final boolean enabled;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "playwright-price-fetcher");
        t.setDaemon(true);
        return t;
    });

    private Playwright playwright;
    private Browser browser;

    public PlaywrightPriceFetcher(@Value("${scraper.playwright.enabled:true}") boolean enabled) {
        this.enabled = enabled;
    }

    public Optional<FetchedPrice> fetch(String url, String... priceSelectors) {
        if (!enabled || url == null || url.isBlank()) {
            return Optional.empty();
        }
        try {
            return executor.submit(() -> doFetch(url, priceSelectors)).get(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("Playwright price fetch failed for {}: {}", url, ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<FetchedPrice> doFetch(String url, String[] priceSelectors) {
        ensureBrowser();
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(USER_AGENT)
                .setViewportSize(1280, 800));
        try {
            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(30000));

            if (priceSelectors.length > 0) {
                try {
                    page.waitForSelector(String.join(", ", priceSelectors),
                            new Page.WaitForSelectorOptions().setTimeout(8000));
                } catch (Exception ignored) {
                }
            }

            String priceText = firstText(page, priceSelectors);
            if (priceText == null) {
                return Optional.empty();
            }
            String name = firstText(page, "h1");
            return Optional.of(new FetchedPrice(priceText, name));
        } finally {
            context.close();
        }
    }

    private synchronized void ensureBrowser() {
        if (browser == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            log.info("Playwright Chromium launched for price fetching.");
        }
    }

    private String firstText(Page page, String... selectors) {
        for (String selector : selectors) {
            ElementHandle el = page.querySelector(selector);
            if (el != null) {
                String text = el.innerText();
                if (text != null && !text.isBlank()) {
                    return text.trim();
                }
            }
        }
        return null;
    }

    @PreDestroy
    public void shutdown() {
        try {
            executor.submit(() -> {
                if (browser != null) browser.close();
                if (playwright != null) playwright.close();
                return null;
            }).get(10, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        executor.shutdownNow();
    }

    public record FetchedPrice(String priceText, String name) {
    }
}
