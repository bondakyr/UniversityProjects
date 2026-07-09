package cz.cvut.fel.nss.productcatalogservice.dto.catalog;

import cz.cvut.fel.nss.productcatalogservice.dto.comment.CommentResponse;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductDetailResponse implements java.io.Serializable {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String parameters;
    private String categoryName;
    private List<PriceHistoryResponse> priceHistory;
    private List<CommentResponse> comments;
    private BigDecimal minPrice;
    private BigDecimal currentLowestPrice;
    private String sourceUrl;
}