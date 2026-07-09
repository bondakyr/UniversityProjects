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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertEvaluationServiceTest {

    @Mock
    private WatchlistItemRepository watchlistItemRepository;
    @Mock
    private PriceAlertRepository priceAlertRepository;
    @Mock
    private PriceDropProducer priceDropProducer;

    @InjectMocks
    private AlertEvaluationService service;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("RAM 32GB");
    }

    private PriceUpdateEvent event(BigDecimal price) {
        PriceUpdateEvent e = new PriceUpdateEvent();
        e.setProductId(1L);
        e.setPrice(price);
        e.setShopName("Alza");
        e.setProductUrl("https://alza.cz/ram");
        return e;
    }

    private WatchlistItem watchlist(Long userId, BigDecimal target) {
        WatchlistItem item = new WatchlistItem();
        item.setUserId(userId);
        item.setProduct(product);
        item.setTargetPrice(target);
        return item;
    }

    @Test
    void emitsPriceDrop_whenWatchlistTargetMet() {
        when(watchlistItemRepository.findByProductId(1L))
                .thenReturn(List.of(watchlist(42L, new BigDecimal("2000"))));
        when(priceAlertRepository.findByProductIdAndActiveTrue(1L)).thenReturn(List.of());

        service.evaluate(product, new BigDecimal("2500"), event(new BigDecimal("1900")));

        ArgumentCaptor<PriceDropEvent> captor = ArgumentCaptor.forClass(PriceDropEvent.class);
        verify(priceDropProducer).publish(captor.capture());
        PriceDropEvent drop = captor.getValue();
        assertThat(drop.getUserId()).isEqualTo(42L);
        assertThat(drop.getProductId()).isEqualTo(1L);
        assertThat(drop.getNewPrice()).isEqualByComparingTo("1900");
        assertThat(drop.getOldPrice()).isEqualByComparingTo("2500");
        assertThat(drop.getThresholdValue()).isEqualByComparingTo("2000");
        assertThat(drop.getEventId()).isNotBlank();
    }

    @Test
    void emitsPriceDrop_whenPriceEqualsTargetExactly() {
        when(watchlistItemRepository.findByProductId(1L))
                .thenReturn(List.of(watchlist(7L, new BigDecimal("2000"))));
        when(priceAlertRepository.findByProductIdAndActiveTrue(1L)).thenReturn(List.of());

        service.evaluate(product, new BigDecimal("2500"), event(new BigDecimal("2000")));

        verify(priceDropProducer).publish(any(PriceDropEvent.class));
    }

    @Test
    void doesNotEmit_whenWatchlistTargetNotMet() {
        when(watchlistItemRepository.findByProductId(1L))
                .thenReturn(List.of(watchlist(42L, new BigDecimal("1500"))));
        when(priceAlertRepository.findByProductIdAndActiveTrue(1L)).thenReturn(List.of());

        service.evaluate(product, new BigDecimal("2500"), event(new BigDecimal("1900")));

        verifyNoInteractions(priceDropProducer);
    }

    @Test
    void ignoresWatchlistItem_withNullTarget() {
        when(watchlistItemRepository.findByProductId(1L))
                .thenReturn(List.of(watchlist(42L, null)));
        when(priceAlertRepository.findByProductIdAndActiveTrue(1L)).thenReturn(List.of());

        service.evaluate(product, new BigDecimal("2500"), event(new BigDecimal("1900")));

        verifyNoInteractions(priceDropProducer);
    }

    @Test
    void emitsPriceDrop_whenAlertDropBelowMet() {
        PriceAlert alert = new PriceAlert();
        alert.setUserId(99L);
        alert.setProduct(product);
        alert.setConditionType(PriceConditionType.DROP_BELOW);
        alert.setThresholdValue(new BigDecimal("2000"));
        alert.setActive(true);

        when(watchlistItemRepository.findByProductId(1L)).thenReturn(List.of());
        when(priceAlertRepository.findByProductIdAndActiveTrue(1L)).thenReturn(List.of(alert));

        service.evaluate(product, new BigDecimal("2500"), event(new BigDecimal("1800")));

        verify(priceDropProducer).publish(any(PriceDropEvent.class));
    }

    @Test
    void emitsPriceDrop_whenAlertDropPercentMet() {
        PriceAlert alert = new PriceAlert();
        alert.setUserId(99L);
        alert.setProduct(product);
        alert.setConditionType(PriceConditionType.DROP_PERCENT);
        alert.setThresholdValue(new BigDecimal("15"));
        alert.setActive(true);

        when(watchlistItemRepository.findByProductId(1L)).thenReturn(List.of());
        when(priceAlertRepository.findByProductIdAndActiveTrue(1L)).thenReturn(List.of(alert));

        service.evaluate(product, new BigDecimal("100"), event(new BigDecimal("80")));

        verify(priceDropProducer).publish(any(PriceDropEvent.class));
    }

    @Test
    void doesNotEmit_whenAlertDropPercentTooSmall() {
        PriceAlert alert = new PriceAlert();
        alert.setUserId(99L);
        alert.setProduct(product);
        alert.setConditionType(PriceConditionType.DROP_PERCENT);
        alert.setThresholdValue(new BigDecimal("25"));
        alert.setActive(true);

        when(watchlistItemRepository.findByProductId(1L)).thenReturn(List.of());
        when(priceAlertRepository.findByProductIdAndActiveTrue(1L)).thenReturn(List.of(alert));

        service.evaluate(product, new BigDecimal("100"), event(new BigDecimal("80")));

        verifyNoInteractions(priceDropProducer);
    }

    @Test
    void doesNothing_whenNewPriceIsNull() {
        service.evaluate(product, new BigDecimal("100"), event(null));

        verifyNoInteractions(priceDropProducer);
        verifyNoInteractions(watchlistItemRepository);
        verifyNoInteractions(priceAlertRepository);
    }
}
