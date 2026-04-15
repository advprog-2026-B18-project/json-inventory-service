package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductCreateRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Long price;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Origin country is required")
    @JsonProperty("origin_country")
    private String originCountry;

    @NotNull(message = "Purchase date is required")
    @JsonProperty("purchase_date")
    private LocalDate purchaseDate;

    @JsonProperty("category_id")
    private Integer categoryId;

    @JsonProperty("weight_gram")
    private Integer weightGram;

    @PositiveOrZero(message = "Service fee cannot be negative")
    @JsonProperty("service_fee")
    private Long serviceFee;

    @Size(max = 5, message = "Maximum 5 images allowed")
    private List<String> images;

    private List<String> tags;
}