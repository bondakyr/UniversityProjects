package cz.cvut.fel.nss.scraperservice.service;

import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.scraperservice.dto.ShopRequest;
import cz.cvut.fel.nss.scraperservice.dto.ShopResponse;
import cz.cvut.fel.nss.scraperservice.entity.Shop;
import cz.cvut.fel.nss.scraperservice.repository.ShopRepository;
import cz.cvut.fel.nss.scraperservice.strategy.ScraperStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ScraperStrategyFactory strategyFactory;

    public List<ShopResponse> list() {
        return shopRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ShopResponse create(ShopRequest request) {
        if (request.getName() == null || request.getStrategyKey() == null) {
            throw new BadRequestException("name and strategyKey are required");
        }
        if (strategyFactory.get(request.getStrategyKey()).isEmpty()) {
            throw new BadRequestException("Unknown strategyKey. Available: " + strategyFactory.registeredKeys());
        }
        Shop shop = new Shop();
        shop.setName(request.getName());
        shop.setBaseUrls(request.getBaseUrls() != null ? request.getBaseUrls() : "");
        shop.setStrategyKey(request.getStrategyKey().toUpperCase());
        shop.setActive(request.getActive() == null || request.getActive());
        shop.setReliabilityScore(request.getReliabilityScore());
        return toResponse(shopRepository.save(shop));
    }

    @Transactional
    public ShopResponse update(Long id, ShopRequest request) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        if (request.getName() != null) shop.setName(request.getName());
        if (request.getBaseUrls() != null) shop.setBaseUrls(request.getBaseUrls());
        if (request.getStrategyKey() != null) {
            if (strategyFactory.get(request.getStrategyKey()).isEmpty()) {
                throw new BadRequestException("Unknown strategyKey");
            }
            shop.setStrategyKey(request.getStrategyKey().toUpperCase());
        }
        if (request.getActive() != null) shop.setActive(request.getActive());
        if (request.getReliabilityScore() != null) shop.setReliabilityScore(request.getReliabilityScore());
        return toResponse(shopRepository.save(shop));
    }

    @Transactional
    public void delete(Long id) {
        if (!shopRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shop not found");
        }
        shopRepository.deleteById(id);
    }

    private ShopResponse toResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .baseUrls(shop.getBaseUrls())
                .strategyKey(shop.getStrategyKey())
                .active(shop.isActive())
                .reliabilityScore(shop.getReliabilityScore())
                .build();
    }
}
