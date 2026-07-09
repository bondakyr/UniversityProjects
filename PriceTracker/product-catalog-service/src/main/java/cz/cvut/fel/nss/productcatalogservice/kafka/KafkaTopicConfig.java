package cz.cvut.fel.nss.productcatalogservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String PRICE_UPDATES_TOPIC = "price-updates";
    public static final String PRICE_DROPS_TOPIC = "price-drops";

    @Bean
    public NewTopic priceUpdatesTopic() {
        return TopicBuilder.name(PRICE_UPDATES_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic priceDropsTopic() {
        return TopicBuilder.name(PRICE_DROPS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
