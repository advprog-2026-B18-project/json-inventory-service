package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class PostOrderRequest {
    private UUID orderId;
    private String action;
    private Double rating;
    private String reviewText;
    private String reason;
}