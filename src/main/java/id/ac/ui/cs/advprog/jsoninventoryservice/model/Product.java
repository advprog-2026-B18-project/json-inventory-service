package id.ac.ui.cs.advprog.jsoninventoryservice.model;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Check(constraints = "stock >= 0")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;

    @Column(name = "jastiper_id", nullable = false)
    private UUID jastiperId;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Positive(message = "Price must be more than 0")
    @Column(nullable = false)
    private Integer price;

    @PositiveOrZero
    @Column(name = "service_fee", nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer serviceFee = 0;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "origin_country", nullable = false)
    private String originCountry;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(name = "weight_gram")
    private Integer weightGram;

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "avg_rating")
    private Float avgRating;

    @Column(name = "total_reviews", nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "total_orders", nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}