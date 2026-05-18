package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryResponse {
    @JsonProperty("category_id")
    private Integer categoryId;
    private String name;
    private String slug;
    private String description;
    @JsonProperty("product_count")
    private Integer productCount;

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .productCount(category.getProductCount())
                .build();
    }
}