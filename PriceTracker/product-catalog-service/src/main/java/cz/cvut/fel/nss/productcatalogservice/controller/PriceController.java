package cz.cvut.fel.nss.productcatalogservice.controller;

import cz.cvut.fel.nss.commonshared.payload.ApiResponse;
import cz.cvut.fel.nss.productcatalogservice.dto.kafka.PriceUpdateEvent;
import cz.cvut.fel.nss.productcatalogservice.kafka.PriceUpdateProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceUpdateProducer priceUpdateProducer;

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<PriceUpdateEvent>> publishPriceUpdate(
            @RequestBody PriceUpdateEvent event) {

        priceUpdateProducer.sendPriceUpdate(event);

        return ResponseEntity.ok(new ApiResponse<>(
                "SUCCESS",
                "Price update published to Kafka topic 'price-updates'",
                event
        ));
    }
}
