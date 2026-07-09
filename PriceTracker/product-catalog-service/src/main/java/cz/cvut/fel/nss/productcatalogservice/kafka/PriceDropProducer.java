package cz.cvut.fel.nss.productcatalogservice.kafka;

import cz.cvut.fel.nss.productcatalogservice.dto.kafka.PriceDropEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceDropProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(PriceDropEvent event) {
        log.info("Publishing price-drop event: productId={}, userId={}, newPrice={}",
                event.getProductId(), event.getUserId(), event.getNewPrice());

        kafkaTemplate.send(
                KafkaTopicConfig.PRICE_DROPS_TOPIC,
                event.getProductId().toString(),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish price-drop: {}", ex.getMessage(), ex);
            } else {
                log.info("Price-drop published: offset={}", result.getRecordMetadata().offset());
            }
        });
    }
}
