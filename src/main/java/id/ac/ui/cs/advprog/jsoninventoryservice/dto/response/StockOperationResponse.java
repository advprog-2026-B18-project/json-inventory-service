package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class StockOperationResponse {
    private UUID productId;
    private int reservedQuantity;
    private int remainingStock;
    private UUID reservationId;
    private String status;
}