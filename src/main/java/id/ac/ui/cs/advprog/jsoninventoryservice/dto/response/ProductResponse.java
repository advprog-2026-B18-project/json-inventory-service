package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
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
    private Long serviceFee;
    private Integer weightGram;

    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .jastiperId(product.getJastiperId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .originCountry(product.getOriginCountry())
                .purchaseDate(product.getPurchaseDate())
                .images(product.getImages() != null ? new ArrayList<>(product.getImages()) : new ArrayList<>())
                .tags(product.getTags() != null ? new ArrayList<>(product.getTags()) : new ArrayList<>())
                .status(product.getStatus().name())
                .avgRating(product.getAvgRating())
                .totalOrders(product.getTotalOrders())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .serviceFee(product.getServiceFee())
                .weightGram(product.getWeightGram())
                .build();
    }
}