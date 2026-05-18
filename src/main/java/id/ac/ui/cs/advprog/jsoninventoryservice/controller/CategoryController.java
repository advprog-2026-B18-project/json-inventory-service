package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.CategoryRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.DuplicateResourceException;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.CategoryService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {
    private final CategoryService categoryService;


    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody CategoryRequest request) {
        try {
            return ResponseUtil.created(categoryService.createCategory(request), "Category created successfully.");
        } catch (IllegalArgumentException e) {
            ApiResponse<CategoryResponse> errorRes = new ApiResponse<>();
            errorRes.setSuccess(false);
            errorRes.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorRes);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable("id") Integer id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseUtil.success(null, "Category successfully deleted.");
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (IllegalStateException e) {
            ApiResponse<Void> errorRes = new ApiResponse<>();
            errorRes.setSuccess(false);
            errorRes.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorRes);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable("id") Integer id,
            @RequestBody CategoryRequest request) {
        try {
            return ResponseUtil.success(categoryService.updateCategory(id, request), "Category updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (DuplicateResourceException e) {
            ApiResponse<CategoryResponse> errorRes = new ApiResponse<>();
            errorRes.setSuccess(false);
            errorRes.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorRes);
        }
    }
}