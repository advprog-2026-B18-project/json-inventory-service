package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Long price;
    private Integer stock;
    private String status;
    @JsonProperty("category_id")
    private Integer categoryId;
    @JsonProperty("weight_gram")
    private Integer weightGram;
    @JsonProperty("service_fee")
    private Long serviceFee;
    private List<String> images;
    private List<String> tags;
}