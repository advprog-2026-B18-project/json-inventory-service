package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private Long price;
    private Integer stock;
    private String originCountry;
    private LocalDate purchaseDate;
}