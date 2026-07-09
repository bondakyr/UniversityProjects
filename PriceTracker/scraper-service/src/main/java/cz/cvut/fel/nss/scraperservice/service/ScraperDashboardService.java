package cz.cvut.fel.nss.scraperservice.service;

import cz.cvut.fel.nss.scraperservice.dto.ScraperDashboardResponse;
import cz.cvut.fel.nss.scraperservice.dto.ScraperLogResponse;
import cz.cvut.fel.nss.scraperservice.entity.ScraperLog;
import cz.cvut.fel.nss.scraperservice.repository.ScraperLogRepository;
import cz.cvut.fel.nss.scraperservice.strategy.ScraperStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScraperDashboardService {

    private final ScraperLogRepository scraperLogRepository;
    private final ScraperStrategyFactory strategyFactory;

    public ScraperDashboardResponse build() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<ScraperLog> last24h = scraperLogRepository.findByStartedAtAfter(since);

        int success = last24h.stream().mapToInt(ScraperLog::getSuccessCount).sum();
        int failure = last24h.stream().mapToInt(ScraperLog::getFailureCount).sum();
        int blocked = last24h.stream().mapToInt(ScraperLog::getBlockedCount).sum();

        List<ScraperLogResponse> recent = scraperLogRepository
                .findTop50ByOrderByStartedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ScraperDashboardResponse.builder()
                .totalRuns24h(last24h.size())
                .successCount24h(success)
                .failureCount24h(failure)
                .blockedCount24h(blocked)
                .recentLogs(recent)
                .registeredStrategies(strategyFactory.registeredKeys())
                .build();
    }

    private ScraperLogResponse toResponse(ScraperLog l) {
        return ScraperLogResponse.builder()
                .id(l.getId())
                .shopName(l.getShop() != null ? l.getShop().getName() : null)
                .startedAt(l.getStartedAt())
                .finishedAt(l.getFinishedAt())
                .successCount(l.getSuccessCount())
                .failureCount(l.getFailureCount())
                .blockedCount(l.getBlockedCount())
                .status(l.getStatus())
                .errorMessage(l.getErrorMessage())
                .build();
    }
}
