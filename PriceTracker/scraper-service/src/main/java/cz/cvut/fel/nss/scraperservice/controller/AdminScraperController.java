package cz.cvut.fel.nss.scraperservice.controller;

import cz.cvut.fel.nss.scraperservice.dto.ScraperDashboardResponse;
import cz.cvut.fel.nss.scraperservice.security.InternalTokenValidator;
import cz.cvut.fel.nss.scraperservice.service.ScraperDashboardService;
import cz.cvut.fel.nss.scraperservice.service.ScrapingOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/scraper")
@RequiredArgsConstructor
public class AdminScraperController {

    private final ScraperDashboardService dashboardService;
    private final ScrapingOrchestrator orchestrator;
    private final InternalTokenValidator internalTokenValidator;

    @GetMapping("/dashboard")
    public ResponseEntity<ScraperDashboardResponse> dashboard(
            @RequestHeader("X-Internal-Token") String internalToken) {
        internalTokenValidator.requireValid(internalToken);
        return ResponseEntity.ok(dashboardService.build());
    }

    @PostMapping("/run")
    public ResponseEntity<Void> triggerRun(
            @RequestHeader("X-Internal-Token") String internalToken) {
        internalTokenValidator.requireValid(internalToken);
        orchestrator.runForAllActiveShops();
        return ResponseEntity.accepted().build();
    }
}
