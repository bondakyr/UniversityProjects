package cz.cvut.fel.nss.scraperservice.service;

import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.scraperservice.dto.ShopRequest;
import cz.cvut.fel.nss.scraperservice.dto.ShopResponse;
import cz.cvut.fel.nss.scraperservice.entity.Shop;
import cz.cvut.fel.nss.scraperservice.repository.ShopRepository;
import cz.cvut.fel.nss.scraperservice.strategy.ScraperStrategy;
import cz.cvut.fel.nss.scraperservice.strategy.ScraperStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;
    @Mock
    private ScraperStrategyFactory strategyFactory;

    @InjectMocks
    private ShopService service;

    @Test
    void create_savesShop_whenStrategyKnown() {
        ShopRequest req = new ShopRequest();
        req.setName("Mironet");
        req.setStrategyKey("alza");
        req.setBaseUrls("https://mironet.cz");

        when(strategyFactory.get("alza")).thenReturn(Optional.of(mock(ScraperStrategy.class)));
        when(shopRepository.save(any(Shop.class))).thenAnswer(inv -> inv.getArgument(0));

        ShopResponse resp = service.create(req);

        assertThat(resp.getName()).isEqualTo("Mironet");
        assertThat(resp.getStrategyKey()).isEqualTo("ALZA");
        assertThat(resp.isActive()).isTrue();
    }

    @Test
    void create_throws_whenStrategyKeyUnknown() {
        ShopRequest req = new ShopRequest();
        req.setName("Mironet");
        req.setStrategyKey("ebay");
        when(strategyFactory.get("ebay")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BadRequestException.class);
        verify(shopRepository, never()).save(any());
    }

    @Test
    void create_throws_whenRequiredFieldsMissing() {
        ShopRequest req = new ShopRequest();
        req.setName("NoStrategy");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(strategyFactory);
    }

    @Test
    void update_throws_whenShopNotFound() {
        when(shopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new ShopRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_throws_whenShopMissing() {
        when(shopRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
