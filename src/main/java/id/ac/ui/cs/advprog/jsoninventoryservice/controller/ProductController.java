package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductSearchCriteria;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
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
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetailPublic(@PathVariable("id") UUID id) {
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
            @PathVariable("id") UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return productService.updateProduct(jastiperId, id, request)
                .map(p -> ResponseUtil.success(p, "Product updated successfully."))
                .orElse(ResponseUtil.notFound("Product not found."));
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable("id") UUID id) {
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
            @RequestParam(required = false, defaultValue = "created_at") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {

        String sortProperty;
        if ("created_at".equals(sortBy)) {
            sortProperty = "createdAt";
        } else if ("purchase_date".equals(sortBy)) {
            sortProperty = "purchaseDate";
        } else if ("rating".equals(sortBy)) {
            sortProperty = "avgRating";
        } else {
            sortProperty = sortBy;
        }

        Sort sort;
        if ("asc".equalsIgnoreCase(order)) {
            sort = Sort.by(sortProperty).ascending();
        } else {
            sort = Sort.by(sortProperty).descending();
        }
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(q)
                .jastiperId(jastiperId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .categoryId(categoryId)
                .originCountry(originCountry)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        Page<ProductResponse> productPage = productService.searchProductsPublic(criteria, pageable);
        return ResponseUtil.success(buildPaginationMap(productPage), "Search results fetched.");
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyCatalog(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @RequestParam(required = false, name = "search") String q,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        ProductStatus filterStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                filterStatus = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Ignored
            }
        }

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .jastiperId(jastiperId)
                .keyword(q)
                .status(filterStatus)
                .build();

        Page<ProductResponse> productPage = productService.getMyCatalog(criteria, pageable);
        return ResponseUtil.success(buildPaginationMap(productPage), "My catalog fetched successfully.");
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @GetMapping("/my/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getMyProductDetail(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable("id") UUID id) {

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