package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductSearchCriteria;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ShoppingMode;
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
            @RequestAttribute(name = "jastiperId") UUID jastiperId,
            @Valid @RequestBody ProductCreateRequest request) {
        return ResponseUtil.created(productService.createProduct(jastiperId, request), "Product created successfully.");
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @RequestAttribute(name = "jastiperId") UUID jastiperId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return productService.updateProduct(jastiperId, id, request)
                .map(p -> ResponseUtil.success(p, "Product updated successfully."))
                .orElse(ResponseUtil.notFound("Product not found."));
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @RequestAttribute(name = "jastiperId") UUID jastiperId,
            @PathVariable("id") UUID id) {
        productService.deleteProduct(jastiperId, id);
        return ResponseUtil.success(null, "Product deleted successfully.");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchProducts(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "jastiper_id", required = false) UUID jastiperId,
            @RequestParam(name = "min_price", required = false) Long minPrice,
            @RequestParam(name = "max_price", required = false) Long maxPrice,
            @RequestParam(name = "category_id", required = false) Integer categoryId,
            @RequestParam(name = "origin_country", required = false) String originCountry,
            @RequestParam(name = "purchase_date_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "purchase_date_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "sortBy", required = false, defaultValue = "created_at") String sortBy,
            @RequestParam(name = "order", required = false, defaultValue = "desc") String order,
            Pageable pageable) {

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

        Sort sort = "asc".equalsIgnoreCase(order) ? Sort.by(sortProperty).ascending() : Sort.by(sortProperty).descending();
        Pageable finalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        ShoppingMode filterMode = null;
        if (mode != null && !mode.trim().isEmpty()) {
            try {
                filterMode = ShoppingMode.valueOf(mode.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseUtil.success(buildPaginationMap(Page.empty(finalPageable)), "Search results fetched.");
            }
        }

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(q)
                .jastiperId(jastiperId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .categoryId(categoryId)
                .originCountry(originCountry)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .mode(filterMode)
                .build();

        Page<ProductResponse> productPage = productService.searchProductsPublic(criteria, finalPageable);
        return ResponseUtil.success(buildPaginationMap(productPage), "Search results fetched.");
    }

    @PreAuthorize("hasRole('JASTIPER')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyCatalog(
            @RequestAttribute(name = "jastiperId") UUID jastiperId,
            @RequestParam(name = "search", required = false) String q,
            @RequestParam(name = "status", required = false) String status,
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
            @RequestAttribute(name = "jastiperId") UUID jastiperId,
            @PathVariable("id") UUID id) {

        return productService.getMyProductDetail(id, jastiperId)
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