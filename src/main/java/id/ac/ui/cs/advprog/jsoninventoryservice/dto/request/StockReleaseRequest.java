package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

@Data
public class StockReleaseRequest {
    @JsonProperty("order_id")
    private UUID orderId;
}