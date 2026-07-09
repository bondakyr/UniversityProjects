package cz.cvut.fel.nss.scraperservice.config;

import cz.cvut.fel.nss.scraperservice.strategy.TrackedProduct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "scraper")
public class TrackedProductsProperties {

    private List<Item> demoProducts = new ArrayList<>();

    @Data
    public static class Item {
        private Long productId;
        private String shopName;
        private String url;
    }

    public List<TrackedProduct> asTrackedProducts() {
        List<TrackedProduct> list = new ArrayList<>(demoProducts.size());
        for (Item i : demoProducts) {
            list.add(new TrackedProduct(i.getProductId(), i.getShopName(), i.getUrl()));
        }
        return list;
    }
}
