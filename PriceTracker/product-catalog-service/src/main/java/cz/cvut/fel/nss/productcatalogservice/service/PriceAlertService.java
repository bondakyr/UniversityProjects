package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.dto.alert.PriceAlertRequest;
import cz.cvut.fel.nss.productcatalogservice.dto.alert.PriceAlertResponse;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceAlert;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceAlertRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PriceAlertResponse create(Long userId, PriceAlertRequest request) {
        if (request.getProductId() == null || request.getConditionType() == null
                || request.getThresholdValue() == null) {
            throw new BadRequestException("productId, conditionType and thresholdValue are required");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        PriceAlert alert = new PriceAlert();
        alert.setUserId(userId);
        alert.setProduct(product);
        alert.setConditionType(request.getConditionType());
        alert.setThresholdValue(request.getThresholdValue());
        alert.setActive(true);
        return toResponse(priceAlertRepository.save(alert));
    }

    public List<PriceAlertResponse> listForUser(Long userId) {
        return priceAlertRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivate(Long userId, Long alertId) {
        PriceAlert alert = priceAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        if (!alert.getUserId().equals(userId)) {
            throw new BadRequestException("Cannot modify another user's alert");
        }
        alert.setActive(false);
        priceAlertRepository.save(alert);
    }

    private PriceAlertResponse toResponse(PriceAlert alert) {
        return PriceAlertResponse.builder()
                .id(alert.getId())
                .productId(alert.getProduct().getId())
                .productName(alert.getProduct().getName())
                .conditionType(alert.getConditionType())
                .thresholdValue(alert.getThresholdValue())
                .active(alert.isActive())
                .build();
    }
}
