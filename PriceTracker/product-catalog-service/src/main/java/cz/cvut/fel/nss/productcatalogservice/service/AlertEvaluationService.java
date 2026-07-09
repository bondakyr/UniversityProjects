package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.productcatalogservice.dto.kafka.PriceDropEvent;
import cz.cvut.fel.nss.productcatalogservice.dto.kafka.PriceUpdateEvent;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceAlert;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceConditionType;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.entity.WatchlistItem;
import cz.cvut.fel.nss.productcatalogservice.kafka.PriceDropProducer;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceAlertRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.WatchlistItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEvaluationService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final PriceDropProducer priceDropProducer;

    public void evaluate(Product product, BigDecimal previousPrice, PriceUpdateEvent event) {
        BigDecimal newPrice = event.getPrice();
        if (newPrice == null) return;

        List<WatchlistItem> watchers = watchlistItemRepository.findByProductId(product.getId());
        for (WatchlistItem item : watchers) {
            BigDecimal target = item.getTargetPrice();
            if (target != null && newPrice.compareTo(target) <= 0) {
                emit(product, item.getUserId(), previousPrice, newPrice, target, event);
            }
        }

        List<PriceAlert> alerts = priceAlertRepository.findByProductIdAndActiveTrue(product.getId());
        for (PriceAlert alert : alerts) {
            if (matches(alert, previousPrice, newPrice)) {
                emit(product, alert.getUserId(), previousPrice, newPrice, alert.getThresholdValue(), event);
            }
        }
    }

    private boolean matches(PriceAlert alert, BigDecimal previousPrice, BigDecimal newPrice) {
        if (alert.getConditionType() == PriceConditionType.DROP_BELOW) {
            return newPrice.compareTo(alert.getThresholdValue()) <= 0;
        }
        if (alert.getConditionType() == PriceConditionType.DROP_PERCENT && previousPrice != null
                && previousPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dropPct = previousPrice.subtract(newPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previousPrice, 2, RoundingMode.HALF_UP);
            return dropPct.compareTo(alert.getThresholdValue()) >= 0;
        }
        return false;
    }

    private void emit(Product product,
                      Long userId,
                      BigDecimal previousPrice,
                      BigDecimal newPrice,
                      BigDecimal threshold,
                      PriceUpdateEvent event) {
        PriceDropEvent drop = PriceDropEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .productId(product.getId())
                .productName(product.getName())
                .userId(userId)
                .oldPrice(previousPrice)
                .newPrice(newPrice)
                .thresholdValue(threshold)
                .shopName(event.getShopName())
                .productUrl(event.getProductUrl())
                .triggeredAt(System.currentTimeMillis())
                .build();
        priceDropProducer.publish(drop);
    }
}
