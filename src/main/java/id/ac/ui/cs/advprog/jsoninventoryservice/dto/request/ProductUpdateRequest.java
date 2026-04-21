package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductUpdateRequest {
    @Size(max = 255)
    private String name;

    @Size(max = 5000)
    private String description;

    @PositiveOrZero
    private Long price;

    @PositiveOrZero
    private Integer stock;

    @JsonProperty("category_id")
    private Integer categoryId;

    @JsonProperty("origin_country")
    private String originCountry;

    @JsonProperty("purchase_date")
    private LocalDate purchaseDate;

    @JsonProperty("service_fee")
    private Long serviceFee;

    @JsonProperty("weight_gram")
    private Integer weightGram;

    private String status;
    private List<String> images;
    private List<String> tags;
}
