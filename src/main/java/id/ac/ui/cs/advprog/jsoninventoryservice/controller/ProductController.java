package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.ProductService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

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
        return productService.updateProduct(jastiperId, id, request)
                .map(p -> ResponseUtil.success(p, "Product updated successfully."))
                .orElse(ResponseUtil.notFound("Product not found or unauthorized to update."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @RequestAttribute("jastiperId") UUID jastiperId,
            @PathVariable UUID id) {
        if (productService.deleteProduct(jastiperId, id)) {
            return ResponseUtil.success(null, "Product deleted successfully.");
        }
        return ResponseUtil.notFound("Product not found or unauthorized for deletion.");
    }

    @PostMapping("/internal/{id}/stock/reserve")
    public ResponseEntity<ApiResponse<ProductResponse>> reserveStock(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        return productService.reserveStock(id, quantity)
                .map(p -> ResponseUtil.success(p, "Stock reserved successfully."))
                .orElse(ResponseUtil.notFound("Product not found or insufficient stock."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            Pageable pageable) {
        return ResponseUtil.success(productService.searchProductsPublic(q, null, minPrice, maxPrice, pageable), "Search results fetched.");
    }
}