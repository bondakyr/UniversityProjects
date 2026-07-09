package cz.cvut.fel.nss.productcatalogservice.dto.watchlist;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WatchlistItemRequest {
    private Long productId;
    private BigDecimal targetPrice;
}
