package cz.cvut.fel.nss.bff.controller;

import cz.cvut.fel.nss.bff.client.ScraperServiceClient;
import cz.cvut.fel.nss.bff.security.AuthenticatedUserResolver;
import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scrape")
@RequiredArgsConstructor
public class BffScrapeController {

    private final ScraperServiceClient scraperServiceClient;
    private final AuthenticatedUserResolver userResolver;

    @PostMapping
    public ResponseEntity<String> run(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        userResolver.resolve(auth)
                .orElseThrow(() -> new BadRequestException("Sign in to run a scrape"));
        return scraperServiceClient.triggerRun();
    }
}
