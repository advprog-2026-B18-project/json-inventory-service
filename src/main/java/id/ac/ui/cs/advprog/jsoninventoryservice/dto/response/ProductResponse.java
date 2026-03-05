package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID productId;
    private UUID jastiperId;
    private String name;
    private String description;
    private Long price;
    private Integer stock;
    private String originCountry;
    private LocalDate purchaseDate;
    private List<String> images;
    private List<String> tags;
    private String status;
    private Double avgRating;
    private Integer totalOrders;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}