package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.dto.alert.PriceAlertRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.alert.PriceAlertResponse;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceAlert;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceConditionType;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceAlertRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
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
class PriceAlertServiceTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PriceAlertService service;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("SSD");
    }

    @Test
    void create_savesActiveAlert() {
        PriceAlertRequest req = new PriceAlertRequest();
        req.setProductId(1L);
        req.setConditionType(PriceConditionType.DROP_BELOW);
        req.setThresholdValue(new BigDecimal("1000"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        PriceAlertResponse resp = service.create(7L, req);

        assertThat(resp.isActive()).isTrue();
        assertThat(resp.getConditionType()).isEqualTo(PriceConditionType.DROP_BELOW);
        assertThat(resp.getThresholdValue()).isEqualByComparingTo("1000");
    }

    @Test
    void create_throws_whenFieldsMissing() {
        PriceAlertRequest req = new PriceAlertRequest();
        req.setProductId(1L);

        assertThatThrownBy(() -> service.create(7L, req))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    void create_throws_whenProductNotFound() {
        PriceAlertRequest req = new PriceAlertRequest();
        req.setProductId(99L);
        req.setConditionType(PriceConditionType.DROP_BELOW);
        req.setThresholdValue(new BigDecimal("10"));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(7L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivate_setsInactive_whenOwned() {
        PriceAlert alert = new PriceAlert();
        alert.setId(20L);
        alert.setUserId(7L);
        alert.setProduct(product);
        alert.setActive(true);
        when(priceAlertRepository.findById(20L)).thenReturn(Optional.of(alert));
        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deactivate(7L, 20L);

        assertThat(alert.isActive()).isFalse();
        verify(priceAlertRepository).save(alert);
    }

    @Test
    void deactivate_throws_whenNotOwner() {
        PriceAlert alert = new PriceAlert();
        alert.setId(20L);
        alert.setUserId(999L);
        alert.setProduct(product);
        when(priceAlertRepository.findById(20L)).thenReturn(Optional.of(alert));

        assertThatThrownBy(() -> service.deactivate(7L, 20L))
                .isInstanceOf(BadRequestException.class);
        verify(priceAlertRepository, never()).save(any());
    }
}
