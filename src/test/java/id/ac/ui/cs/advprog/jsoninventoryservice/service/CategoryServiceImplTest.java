package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.CategoryRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.CategoryInUseException;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.DuplicateResourceException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Category;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryServiceImpl categoryService;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1);
        category.setName("Electronic");
        category.setSlug("electronic");
        category.setProductCount(0);
    }

    @Test
    void createCategory_Success_AutoSlug() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Short Shirt");
        when(categoryRepository.existsByNameIgnoreCase("Short Shirt")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArguments()[0]);
        CategoryResponse res = categoryService.createCategory(req);
        assertEquals("short-shirt", res.getSlug());
    }

    @Test
    void createCategory_Fail_NameExists() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Electronic");
        when(categoryRepository.existsByNameIgnoreCase("Electronic")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(req));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        categoryService.deleteCategory(1);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_Fail_InUse() {
        category.setProductCount(5);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        assertThrows(CategoryInUseException.class, () -> categoryService.deleteCategory(1));
    }

    @Test
    void deleteCategory_Fail_NotFound() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> categoryService.deleteCategory(1));
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        List<CategoryResponse> res = categoryService.getAllCategories();
        assertEquals(1, res.size());
        assertEquals("Electronic", res.getFirst().getName());
    }

    @Test
    void createCategory_Success_WithExplicitSlug() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Old Pants");
        when(categoryRepository.existsByNameIgnoreCase("Old Pants")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArguments()[0]);
        CategoryResponse res = categoryService.createCategory(req);
        assertEquals("old-pants", res.getSlug());
    }
    @Test
    void testUpdateCategory_Success() {
        Category existingCategory = new Category();
        existingCategory.setCategoryId(1);
        existingCategory.setName("Old Name");
        existingCategory.setSlug("old-name");
        existingCategory.setDescription("Old Desc");

        CategoryRequest request = new CategoryRequest();
        request.setName("New Name");
        request.setDescription("New Desc");

        when(categoryRepository.findById(1)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameIgnoreCase("New Name")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArguments()[0]);

        CategoryResponse response = categoryService.updateCategory(1, request);
        assertNotNull(response);
        assertEquals("New Name", response.getName());
        assertEquals("new-name", response.getSlug());
        assertEquals("New Desc", response.getDescription());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_DuplicateName_ThrowsException() {
        Category existingCategory = new Category();
        existingCategory.setCategoryId(1);
        existingCategory.setName("Old Name");

        CategoryRequest request = new CategoryRequest();
        request.setName("Existing Name");

        when(categoryRepository.findById(1)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameIgnoreCase("Existing Name")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> {
            categoryService.updateCategory(1, request);
        });
    }

    @Test
    void testUpdateCategory_NotFound() {
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());
        CategoryRequest req = new CategoryRequest();
        assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(99, req));
    }

    @Test
    void testUpdateCategory_NullNameAndDescription() {
        Category cat = new Category();
        cat.setName("Old Name");
        cat.setDescription("Old Desc");
        CategoryRequest req = new CategoryRequest();
        req.setName(null);
        req.setDescription(null);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(any())).thenReturn(cat);
        CategoryResponse res = categoryService.updateCategory(1, req);
        assertEquals("Old Name", res.getName());
    }

    @Test
    void testUpdateCategory_EmptyNameString() {
        Category cat = new Category();
        cat.setName("Old Name");
        CategoryRequest req = new CategoryRequest();
        req.setName("    ");

        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(any())).thenReturn(cat);
        categoryService.updateCategory(1, req);
        assertEquals("Old Name", cat.getName());
    }

    @Test
    void testUpdateCategory_SameNameDifferentCase_DoesNotThrowDuplicate() {
        Category cat = new Category();
        cat.setName("Fashion");
        CategoryRequest req = new CategoryRequest();
        req.setName("FASHION");

        when(categoryRepository.findById(1)).thenReturn(java.util.Optional.of(cat));
        when(categoryRepository.save(any())).thenReturn(cat);
        categoryService.updateCategory(1, req);
        assertEquals("FASHION", cat.getName());
    }
}