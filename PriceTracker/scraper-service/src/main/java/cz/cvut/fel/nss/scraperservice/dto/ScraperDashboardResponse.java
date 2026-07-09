package cz.cvut.fel.nss.scraperservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScraperDashboardResponse {
    private int totalRuns24h;
    private int successCount24h;
    private int failureCount24h;
    private int blockedCount24h;
    private List<ScraperLogResponse> recentLogs;
    private List<String> registeredStrategies;
}
