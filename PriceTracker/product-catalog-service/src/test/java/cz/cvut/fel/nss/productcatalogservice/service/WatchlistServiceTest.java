package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.dto.watchlist.WatchlistItemRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.watchlist.WatchlistItemResponse;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceRecord;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.entity.WatchlistItem;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceRecordRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.WatchlistItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchlistItemRepository watchlistItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PriceRecordRepository priceRecordRepository;

    @InjectMocks
    private WatchlistService service;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("GPU");
    }

    @Test
    void add_createsNewItem_whenNoneExists() {
        WatchlistItemRequest req = new WatchlistItemRequest();
        req.setProductId(1L);
        req.setTargetPrice(new BigDecimal("999"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(watchlistItemRepository.findByUserIdAndProductId(5L, 1L)).thenReturn(Optional.empty());
        when(priceRecordRepository.findTopByProductIdOrderByScrapedAtDesc(1L)).thenReturn(Optional.empty());
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenAnswer(inv -> inv.getArgument(0));

        WatchlistItemResponse resp = service.add(5L, req);

        assertThat(resp.getProductId()).isEqualTo(1L);
        assertThat(resp.getTargetPrice()).isEqualByComparingTo("999");
        verify(watchlistItemRepository).save(any(WatchlistItem.class));
    }

    @Test
    void add_updatesTarget_whenItemAlreadyExists() {
        WatchlistItem existing = new WatchlistItem();
        existing.setId(10L);
        existing.setUserId(5L);
        existing.setProduct(product);
        existing.setTargetPrice(new BigDecimal("500"));

        WatchlistItemRequest req = new WatchlistItemRequest();
        req.setProductId(1L);
        req.setTargetPrice(new BigDecimal("450"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(watchlistItemRepository.findByUserIdAndProductId(5L, 1L)).thenReturn(Optional.of(existing));
        when(priceRecordRepository.findTopByProductIdOrderByScrapedAtDesc(1L)).thenReturn(Optional.empty());
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenAnswer(inv -> inv.getArgument(0));

        WatchlistItemResponse resp = service.add(5L, req);

        assertThat(resp.getTargetPrice()).isEqualByComparingTo("450");
        assertThat(existing.getTargetPrice()).isEqualByComparingTo("450");
    }

    @Test
    void add_includesCurrentPrice_fromLatestRecord() {
        PriceRecord latest = new PriceRecord();
        latest.setPrice(new BigDecimal("1234"));

        WatchlistItemRequest req = new WatchlistItemRequest();
        req.setProductId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(watchlistItemRepository.findByUserIdAndProductId(5L, 1L)).thenReturn(Optional.empty());
        when(priceRecordRepository.findTopByProductIdOrderByScrapedAtDesc(1L)).thenReturn(Optional.of(latest));
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenAnswer(inv -> inv.getArgument(0));

        WatchlistItemResponse resp = service.add(5L, req);

        assertThat(resp.getCurrentPrice()).isEqualByComparingTo("1234");
    }

    @Test
    void add_throws_whenProductIdMissing() {
        WatchlistItemRequest req = new WatchlistItemRequest();

        assertThatThrownBy(() -> service.add(5L, req))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    void add_throws_whenProductNotFound() {
        WatchlistItemRequest req = new WatchlistItemRequest();
        req.setProductId(99L);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(5L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void remove_deletes_whenOwnedByUser() {
        WatchlistItem item = new WatchlistItem();
        item.setId(10L);
        item.setUserId(5L);
        item.setProduct(product);
        when(watchlistItemRepository.findById(10L)).thenReturn(Optional.of(item));

        service.remove(5L, 10L);

        verify(watchlistItemRepository).delete(item);
    }

    @Test
    void remove_throws_whenItemBelongsToAnotherUser() {
        WatchlistItem item = new WatchlistItem();
        item.setId(10L);
        item.setUserId(999L);
        item.setProduct(product);
        when(watchlistItemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.remove(5L, 10L))
                .isInstanceOf(BadRequestException.class);
        verify(watchlistItemRepository, never()).delete(any());
    }
}
