package cz.cvut.fel.nss.productcatalogservice.dto.comment;

import lombok.Data;

@Data
public class CommentRequest {
    private Long productId;
    private String text;
}
