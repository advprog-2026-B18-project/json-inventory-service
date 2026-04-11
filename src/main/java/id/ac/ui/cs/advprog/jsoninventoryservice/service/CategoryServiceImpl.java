package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Category;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .slug(category.getSlug())
                .productCount(category.getProductCount())
                .build();
    }
}