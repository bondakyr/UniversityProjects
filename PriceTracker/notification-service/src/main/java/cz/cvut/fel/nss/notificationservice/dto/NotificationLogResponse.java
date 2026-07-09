package cz.cvut.fel.nss.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationLogResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private String message;
    private LocalDateTime sentAt;
    private String status;
    private String channel;
}
