package cz.cvut.fel.nss.productcatalogservice.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCT_DETAILS_CACHE = "productDetails";

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setClusterName("pricetracker-catalog");
        config.setInstanceName("pricetracker-catalog-cache");

        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);

        MapConfig productDetails = new MapConfig(PRODUCT_DETAILS_CACHE)
                .setTimeToLiveSeconds(600)
                .setEvictionConfig(new EvictionConfig()
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                        .setSize(1000));
        config.addMapConfig(productDetails);

        return config;
    }
}
