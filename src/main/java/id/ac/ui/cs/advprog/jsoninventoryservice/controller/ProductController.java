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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProductsPublic() {
        return ResponseUtil.success(productService.getAllProductsPublic(), "Successfully fetched public catalog.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetailPublic(@PathVariable UUID id) {
        return productService.getProductById(id)
                .map(p -> ResponseUtil.success(p, "Successfully fetched product details."))
                .orElse(ResponseUtil.notFound("Product not found with ID: " + id));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyProducts(@RequestHeader("X-User-Id") UUID jastiperId) {
        return ResponseUtil.success(productService.getMyProducts(jastiperId), "Successfully fetched Jastiper's product list.");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestHeader("X-User-Id") UUID jastiperId,
            @RequestBody ProductCreateRequest request) {
        return ResponseUtil.created(productService.createProduct(jastiperId, request), "Product created successfully.");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @RequestHeader("X-User-Id") UUID jastiperId,
            @PathVariable UUID id,
            @RequestBody ProductUpdateRequest request) {
        return productService.updateProduct(jastiperId, id, request)
                .map(p -> ResponseUtil.success(p, "Product updated successfully."))
                .orElse(ResponseUtil.notFound("Product not found or unauthorized to update."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @RequestHeader("X-User-Id") UUID jastiperId,
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
}