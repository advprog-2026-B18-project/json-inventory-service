package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.UUID;

@Data
public class StockReleaseRequest {
    @JsonProperty("order_id")
    private UUID orderId;

    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
}