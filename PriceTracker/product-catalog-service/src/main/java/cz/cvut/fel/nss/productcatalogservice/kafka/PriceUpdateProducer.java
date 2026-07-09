package cz.cvut.fel.nss.productcatalogservice.kafka;

import cz.cvut.fel.nss.productcatalogservice.dto.kafka.PriceUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceUpdateProducer {

    private final KafkaTemplate<String, PriceUpdateEvent> kafkaTemplate;

    public void sendPriceUpdate(PriceUpdateEvent event) {
        log.info("Publishing price update to Kafka: productId={}, price={}, shop={}",
                event.getProductId(), event.getPrice(), event.getShopName());

        kafkaTemplate.send(KafkaTopicConfig.PRICE_UPDATES_TOPIC, event.getProductId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send price update for productId={}: {}",
                                event.getProductId(), ex.getMessage());
                    } else {
                        log.info("Price update sent successfully: offset={}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
