package cz.cvut.fel.nss.scraperservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdatePublisher {

    public static final String TOPIC = "price-updates";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(PriceUpdateEvent event) {
        log.info("Scraper publishes price update: productId={}, price={}, shop={}",
                event.getProductId(), event.getPrice(), event.getShopName());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getProductId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed publishing price-update: {}", ex.getMessage(), ex);
                    }
                });
    }
}
