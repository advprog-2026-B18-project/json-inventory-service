package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;

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
}