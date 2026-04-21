package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.AdminProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.AdminProductService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final AdminProductService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllProductsAdmin(
        @RequestParam(required = false) String q,
           @RequestParam(required = false) UUID jastiperId,
           @RequestParam(required = false) String status,
           @RequestParam(required = false) Integer categoryId,
           Pageable pageable) {

        Page<ProductResponse> productPage = adminService.getAllProductsAdmin(q, jastiperId, status, categoryId, pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", productPage.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", productPage.getNumber() + 1);
        pagination.put("limit", productPage.getSize());
        pagination.put("total", productPage.getTotalElements());
        pagination.put("total_pages", productPage.getTotalPages());
        responseData.put("pagination", pagination);

        return ResponseUtil.success(responseData, "Product list successfully retrieved by Admin.");
    }

    @PatchMapping("/{id}/moderate")
    public ResponseEntity<ApiResponse<ProductResponse>> moderateProduct(
           @RequestAttribute("adminId") UUID adminId,
           @PathVariable UUID id,
           @RequestBody AdminProductUpdateRequest request) {

        try {
            return adminService.moderateProduct(adminId, id, request)
                    .map(p -> ResponseUtil.success(p, "Moderation successfully recorded."))
                    .orElse(ResponseUtil.notFound("Product not found."));
        } catch (IllegalArgumentException e) {
            ApiResponse<ProductResponse> errorRes = new ApiResponse<>();
            errorRes.setSuccess(false);
            errorRes.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorRes);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getAdminProductDetail(@PathVariable UUID id) {
        return adminService.getAdminProductDetail(id)
                .map(p -> ResponseUtil.success(p, "Admin product details successfully retrieved."))
                .orElse(ResponseUtil.notFound("Product not found."));
    }
}