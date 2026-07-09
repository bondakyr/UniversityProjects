package cz.cvut.fel.nss.productcatalogservice.controller;

import cz.cvut.fel.nss.productcatalogservice.dto.catalog.ScrapeTargetResponse;
import cz.cvut.fel.nss.productcatalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalCatalogController {

    private final ProductService productService;

    @GetMapping("/scrape-targets")
    public List<ScrapeTargetResponse> scrapeTargets() {
        return productService.getScrapeTargets();
    }
}
