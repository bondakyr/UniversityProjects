package cz.cvut.fel.nss.productcatalogservice.dto.watchlist;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WatchlistItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal targetPrice;
    private BigDecimal currentPrice;
    private LocalDateTime createdAt;
}
