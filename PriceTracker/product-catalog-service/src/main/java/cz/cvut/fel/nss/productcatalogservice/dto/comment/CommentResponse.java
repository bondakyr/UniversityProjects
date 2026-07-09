package cz.cvut.fel.nss.productcatalogservice.dto.comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse implements java.io.Serializable {
    private Long id;
    private Long userId;
    private String userLogin;
    private String text;
    private LocalDateTime createdAt;
}
