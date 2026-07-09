package cz.cvut.fel.nss.scraperservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScraperLogResponse {
    private Long id;
    private String shopName;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private int successCount;
    private int failureCount;
    private int blockedCount;
    private String status;
    private String errorMessage;
}
