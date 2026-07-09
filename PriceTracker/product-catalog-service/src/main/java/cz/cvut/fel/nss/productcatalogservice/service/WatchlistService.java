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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final ProductRepository productRepository;
    private final PriceRecordRepository priceRecordRepository;

    @Transactional
    public WatchlistItemResponse add(Long userId, WatchlistItemRequest request) {
        if (request.getProductId() == null) {
            throw new BadRequestException("productId is required");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        WatchlistItem item = watchlistItemRepository
                .findByUserIdAndProductId(userId, product.getId())
                .orElseGet(() -> {
                    WatchlistItem fresh = new WatchlistItem();
                    fresh.setUserId(userId);
                    fresh.setProduct(product);
                    return fresh;
                });
        item.setTargetPrice(request.getTargetPrice());

        return toResponse(watchlistItemRepository.save(item));
    }

    public List<WatchlistItemResponse> list(Long userId) {
        return watchlistItemRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void remove(Long userId, Long itemId) {
        WatchlistItem item = watchlistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist item not found"));
        if (!item.getUserId().equals(userId)) {
            throw new BadRequestException("Cannot remove another user's watchlist item");
        }
        watchlistItemRepository.delete(item);
    }

    private WatchlistItemResponse toResponse(WatchlistItem item) {
        BigDecimal current = priceRecordRepository
                .findTopByProductIdOrderByScrapedAtDesc(item.getProduct().getId())
                .map(PriceRecord::getPrice)
                .orElse(null);

        return WatchlistItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .imageUrl(item.getProduct().getImageUrl())
                .targetPrice(item.getTargetPrice())
                .currentPrice(current)
                .createdAt(item.getCreatedAt())
                .build();
    }
}
