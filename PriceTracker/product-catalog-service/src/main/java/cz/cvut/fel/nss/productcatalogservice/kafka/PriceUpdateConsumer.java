package cz.cvut.fel.nss.productcatalogservice.kafka;

import cz.cvut.fel.nss.productcatalogservice.dto.kafka.PriceUpdateEvent;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceRecord;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceRecordRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
import cz.cvut.fel.nss.productcatalogservice.search.ProductSearchService;
import cz.cvut.fel.nss.productcatalogservice.service.AlertEvaluationService;
import cz.cvut.fel.nss.productcatalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdateConsumer {

    private final PriceRecordRepository priceRecordRepository;
    private final ProductRepository productRepository;
    private final AlertEvaluationService alertEvaluationService;
    private final ProductSearchService productSearchService;
    private final ProductService productService;

    @KafkaListener(
            topics = KafkaTopicConfig.PRICE_UPDATES_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void consume(PriceUpdateEvent event) {
        log.info("Received price update from Kafka: productId={}, price={}, shop={}",
                event.getProductId(), event.getPrice(), event.getShopName());

        Product product = productRepository.findById(event.getProductId()).orElse(null);
        if (product == null) {
            log.warn("PriceUpdateConsumer: product not found for id={}, skipping.", event.getProductId());
            return;
        }

        if ("Auto Tracked Product".equals(product.getName()) && event.getProductName() != null) {
            product.setName(event.getProductName());
            productRepository.save(product);
        }

        Optional<PriceRecord> previousLatest =
                priceRecordRepository.findTopByProductIdOrderByScrapedAtDesc(event.getProductId());
        BigDecimal previousPrice = previousLatest.map(PriceRecord::getPrice).orElse(null);

        PriceRecord record = new PriceRecord();
        record.setProduct(product);
        record.setPrice(event.getPrice());
        record.setShopName(event.getShopName() != null ? event.getShopName() : "Unknown");
        record.setProductUrl(event.getProductUrl());
        record.setStockCount(event.getStockCount());

        priceRecordRepository.save(record);
        productSearchService.indexProduct(product, event.getPrice(), record.getShopName());
        productService.evictProductDetail(product.getId());

        log.info("PriceRecord saved for productId={}, price={}", event.getProductId(), event.getPrice());

        alertEvaluationService.evaluate(product, previousPrice, event);
    }
}
