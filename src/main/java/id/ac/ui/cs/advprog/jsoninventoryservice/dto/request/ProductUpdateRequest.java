package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Long price;
    private Integer stock;
    private String status;
}