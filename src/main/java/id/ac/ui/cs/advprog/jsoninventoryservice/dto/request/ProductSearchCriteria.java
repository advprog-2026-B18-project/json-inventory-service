package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ProductSearchCriteria {
    private String keyword;
    private UUID jastiperId;
    private Long minPrice;
    private Long maxPrice;
    private Integer categoryId;
    private ProductStatus status;
    private String originCountry;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}