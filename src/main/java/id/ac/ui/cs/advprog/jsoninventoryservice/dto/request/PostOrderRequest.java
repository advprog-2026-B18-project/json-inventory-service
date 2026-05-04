package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PostOrderRequest {
    @JsonProperty("order_id")
    private UUID orderId;
    private String action;
    private Double rating;
    @JsonProperty("review_text")
    private String reviewText;
    @JsonProperty("product_images")
    private List<String> productImages;
}