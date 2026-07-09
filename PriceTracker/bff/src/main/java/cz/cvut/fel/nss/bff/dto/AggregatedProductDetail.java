package cz.cvut.fel.nss.bff.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AggregatedProductDetail {
    private JsonNode product;
    private JsonNode comments;
    private Boolean inWatchlist;
}
