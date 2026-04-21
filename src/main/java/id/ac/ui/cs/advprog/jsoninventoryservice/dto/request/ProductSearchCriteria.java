package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ProductSearchCriteria {
    private String keyword;
    @JsonProperty("jastiper_id")
    private UUID jastiperId;
    private Long minPrice;
    private Long maxPrice;
    @JsonProperty("category_id")
    private Integer categoryId;
    private ProductStatus status;
    @JsonProperty("origin_country")
    private String originCountry;
    @JsonProperty("date_from")
    private LocalDate dateFrom;
    @JsonProperty("date_to")
    private LocalDate dateTo;
}