package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.ProductService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;

import java.time.LocalDate;
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
        return productService.getProductById(id).map(p -> ResponseUtil.success(p, "Successfully fetched product details."))
                .orElse(ResponseUtil.notFound("Product not found with ID: " + id));
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @Valid @RequestBody ProductCreateRequest request) {
        return ResponseUtil.created(productService.createProduct(jastiperId, request), "Product created successfully.");
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return productService.updateProduct(jastiperId, id, request)
                .map(p -> ResponseUtil.success(p, "Product updated successfully."))
                .orElse(ResponseUtil.notFound("Product not found."));
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable UUID id) {
        productService.deleteProduct(jastiperId, id);
        return ResponseUtil.success(null, "Product deleted successfully.");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID jastiperId,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, name = "origin_country") String originCountry,
            @RequestParam(required = false, name = "purchase_date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false, name = "purchase_date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "created_at") String sort_by,
            @RequestParam(required = false, defaultValue = "desc") String order) {

        String sortProperty = sort_by.equals("created_at") ? "createdAt" : sort_by.equals("purchase_date") ? "purchaseDate" : sort_by.equals("rating") ? "avgRating" : sort_by;
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortProperty).ascending() : Sort.by(sortProperty).descending();
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        Page<ProductResponse> productPage = productService.searchProductsPublic(q, jastiperId, minPrice, maxPrice, categoryId, originCountry, dateFrom, dateTo, pageable);

        return ResponseUtil.success(buildPaginationMap(productPage), "Search results fetched.");
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyCatalog(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @RequestParam(required = false, name = "search") String q,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Page<ProductResponse> productPage = productService.getMyCatalog(jastiperId, q, status, pageable);
        return ResponseUtil.success(buildPaginationMap(productPage), "My catalog fetched successfully.");
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @GetMapping("/my/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getMyProductDetail(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable UUID id) {

        return productService.getProductById(id)
                .filter(p -> p.getJastiper() != null && p.getJastiper().getUserId().equals(jastiperId))
                .map(res -> ResponseUtil.success(res, "My product detail fetched."))
                .orElse(ResponseUtil.notFound("Product not found or does not belong to you."));
    }

    private Map<String, Object> buildPaginationMap(Page<ProductResponse> productPage) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", productPage.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", productPage.getNumber() + 1);
        pagination.put("limit", productPage.getSize());
        pagination.put("total", productPage.getTotalElements());
        pagination.put("total_pages", productPage.getTotalPages());
        responseData.put("pagination", pagination);

        return responseData;
    }
}