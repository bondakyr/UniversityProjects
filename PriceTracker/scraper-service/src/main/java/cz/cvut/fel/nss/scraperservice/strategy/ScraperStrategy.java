package cz.cvut.fel.nss.scraperservice.strategy;

import cz.cvut.fel.nss.scraperservice.entity.Shop;

import java.util.List;

public interface ScraperStrategy {

    String key();

    List<ScrapedItem> fetchPrices(Shop shop, List<TrackedProduct> products) throws Exception;
}
