package cz.cvut.fel.nss.notificationservice.kafka;

import cz.cvut.fel.nss.notificationservice.service.PriceDropNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceDropConsumer {

    public static final String TOPIC = "price-drops";

    private final PriceDropNotificationService priceDropNotificationService;

    @KafkaListener(topics = TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PriceDropEvent event) {
        log.info("Consumed price-drop event: productId={}, userId={}, newPrice={}",
                event.getProductId(), event.getUserId(), event.getNewPrice());
        try {
            priceDropNotificationService.handle(event);
        } catch (Exception ex) {
            log.error("Failed to handle price-drop event {}: {}", event.getEventId(), ex.getMessage(), ex);
        }
    }
}
