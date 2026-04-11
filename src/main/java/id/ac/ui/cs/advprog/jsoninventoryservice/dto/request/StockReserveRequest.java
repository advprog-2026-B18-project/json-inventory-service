package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;
import jakarta.validation.constraints.Positive;

@Data
public class StockReserveRequest {

    @JsonProperty("order_id")
    private UUID orderId;

    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
}