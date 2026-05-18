package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class StockOperationResponse {
    @JsonProperty("product_id")
    private UUID productId;
    @JsonProperty("reserved_quantity")
    private int reservedQuantity;
    @JsonProperty("remaining_stock")
    private int remainingStock;
    @JsonProperty("reservation_id")
    private UUID reservationId;
    private String status;
}