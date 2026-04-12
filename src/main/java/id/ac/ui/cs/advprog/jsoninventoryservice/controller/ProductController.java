package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.ProductService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetailPublic(@PathVariable UUID id) {
        return productService.getProductById(id)
                .map(p -> ResponseUtil.success(p, "Successfully fetched product details."))
                .orElse(ResponseUtil.notFound("Product not found with ID: " + id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @RequestBody ProductCreateRequest request) {
        return ResponseUtil.created(productService.createProduct(jastiperId, request), "Product created successfully.");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable UUID id,
            @RequestBody ProductUpdateRequest request) {
        try {
            return productService.updateProduct(jastiperId, id, request)
                    .map(p -> ResponseUtil.success(p, "Product updated successfully."))
                    .orElse(ResponseUtil.notFound("Product not found or unauthorized to update."));
        } catch (IllegalArgumentException e) {
            ApiResponse<ProductResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable UUID id) {
        try {
            if (productService.deleteProduct(jastiperId, id)) {
                return ResponseUtil.success(null, "Product deleted successfully.");
            }
            return ResponseUtil.notFound("Product not found or unauthorized for deletion.");
        } catch (IllegalStateException e) {
            ApiResponse<Void> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(409).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID jastiperId,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Integer categoryId,
            Pageable pageable) {

        Page<ProductResponse> productPage = productService.searchProductsPublic(q, jastiperId, minPrice, maxPrice, categoryId, pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", productPage.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", productPage.getNumber() + 1);
        pagination.put("limit", productPage.getSize());
        pagination.put("total", productPage.getTotalElements());
        pagination.put("total_pages", productPage.getTotalPages());

        responseData.put("pagination", pagination);

        return ResponseUtil.success(responseData, "Search results fetched.");
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyCatalog(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @RequestParam(required = false, name = "search") String q,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Page<ProductResponse> productPage = productService.getMyCatalog(jastiperId, q, status, pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", productPage.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", productPage.getNumber() + 1);
        pagination.put("limit", productPage.getSize());
        pagination.put("total", productPage.getTotalElements());
        pagination.put("total_pages", productPage.getTotalPages());

        responseData.put("pagination", pagination);

        return ResponseUtil.success(responseData, "My catalog fetched successfully.");
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getMyProductDetail(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable UUID id) {

        return productService.getProductById(id)
                .filter(p -> p.getJastiperId().equals(jastiperId))
                .map(res -> ResponseUtil.success(res, "My product detail fetched."))
                .orElse(ResponseUtil.notFound("Product not found or does not belong to you."));
    }
}