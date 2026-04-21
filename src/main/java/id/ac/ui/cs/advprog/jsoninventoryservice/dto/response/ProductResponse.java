package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    @JsonProperty("product_id")
    private UUID productId;
    private String name;
    private String description;
    private long price;
    private int stock;
    private String status;
    @JsonProperty("origin_country")
    private String originCountry;
    @JsonProperty("purchase_date")
    private LocalDate purchaseDate;
    @JsonProperty("weight_gram")
    private Integer weightGram;
    @JsonProperty("service_fee")
    private Long serviceFee;
    private List<String> images;
    private List<String> tags;
    private CategoryInfo category;
    private JastiperInfo jastiper;
    private ProductStats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Integer id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JastiperInfo {
        private UUID userId;
        private String username;
        private String fullName;
        private String profilePictureUrl;
        private Double avgRating;
        private Integer totalOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStats {
        private Integer totalOrders;
        private Integer totalReviews;
        private Double avgRating;
    }

    public static ProductResponse fromEntity(Product p) {
        return ProductResponse.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .originCountry(p.getOriginCountry())
                .purchaseDate(p.getPurchaseDate())
                .weightGram(p.getWeightGram())
                .serviceFee(p.getServiceFee() != null ? p.getServiceFee().longValue() : null)
                .images(p.getImages())
                .tags(p.getTags())
                .stats(ProductStats.builder()
                        .totalOrders(p.getTotalOrders() != null ? p.getTotalOrders() : 0)
                        .totalReviews(p.getTotalReviews() != null ? p.getTotalReviews() : 0)
                        .avgRating(p.getAvgRating() != null ? p.getAvgRating() : 0.0)
                        .build())
                .jastiper(JastiperInfo.builder().userId(p.getJastiperId()).build())
                .category(p.getCategoryId() != null ? CategoryInfo.builder().id(p.getCategoryId()).build() : null)
                .build();
    }
}