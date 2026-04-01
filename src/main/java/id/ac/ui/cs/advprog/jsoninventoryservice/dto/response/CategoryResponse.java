package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Integer categoryId;
    private String name;
    private String slug;
    private Integer productCount;
}