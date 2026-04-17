package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

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

    private String status;
    private Integer categoryId;
    private String originCountry;
    private LocalDate purchaseDate;
    private Long serviceFee;
    private Integer weightGram;
    private List<String> images;
    private List<String> tags;
}