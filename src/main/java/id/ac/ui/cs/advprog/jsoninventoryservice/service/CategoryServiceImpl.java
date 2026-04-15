package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.CategoryRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.CategoryInUseException;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.DuplicateResourceException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Category;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists.");
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(request.getName().toLowerCase().replace(" ", "-"));
        category.setDescription(request.getDescription());

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (category.getProductCount() > 0) {
            throw new CategoryInUseException(category.getProductCount());
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (!category.getName().equalsIgnoreCase(request.getName()) && categoryRepository.existsByNameIgnoreCase(request.getName())) {
                throw new DuplicateResourceException("Category name already exists.");
            }
            category.setName(request.getName());
            category.setSlug(request.getName().toLowerCase().replace(" ", "-"));
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }
}