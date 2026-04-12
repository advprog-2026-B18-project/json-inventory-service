package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private Long price;
    private Integer stock;
    @JsonProperty("origin_country")
    private String originCountry;
    @JsonProperty("purchase_date")
    private LocalDate purchaseDate;
    @JsonProperty("category_id")
    private Integer categoryId;
    @JsonProperty("weight_gram")
    private Integer weightGram;
    @JsonProperty("service_fee")
    private Long serviceFee;
    private List<String> images;
    private List<String> tags;
}