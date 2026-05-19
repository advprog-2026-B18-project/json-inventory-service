package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.CategoryRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse createCategory(CategoryRequest request);
    void deleteCategory(Integer id);
    CategoryResponse updateCategory(Integer id, CategoryRequest request);
}