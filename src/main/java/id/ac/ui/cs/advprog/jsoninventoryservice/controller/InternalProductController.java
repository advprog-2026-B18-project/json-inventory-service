package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.PostOrderRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReleaseRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReserveRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.StockOperationResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.StockOperationException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.StockManagementService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {
    private final StockManagementService stockService;
    private final ProductRepository productRepository;

    @PostMapping("/{id}/stock/reserve")
    public ResponseEntity<StockOperationResponse> reserveStock(@PathVariable UUID id, @Valid @RequestBody StockReserveRequest request) {
        Product product = productRepository.findByIdForUpdate(id).orElseThrow(() -> new StockOperationException("Product not found", 404));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new StockOperationException("Product is not available for purchase", 422);
        }
        if (product.getStock() < request.getQuantity()) {
            throw new StockOperationException("Insufficient stock", 409);
        }

        StockOperationResponse response = stockService.reserveStock(id, request).orElseThrow(() -> new StockOperationException("Reservation failed", 500));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stock/release")
    public ResponseEntity<ApiResponse<ProductResponse>> releaseStock(@PathVariable UUID id, @Valid @RequestBody StockReleaseRequest request) {
        return stockService.releaseStock(id, request)
                .map(p -> ResponseUtil.success(p, "Stock released successfully."))
                .orElse(ResponseUtil.notFound("Reservation not found or already released."));
    }

    @PostMapping("/{id}/post-order")
    public ResponseEntity<ApiResponse<ProductResponse>> processPostOrder(
            @PathVariable UUID id,
            @Valid @RequestBody PostOrderRequest request) {

        try {
            return stockService.processPostOrder(id, request)
                    .map(p -> ResponseUtil.success(p, "Post-order processed successfully."))
                    .orElse(ResponseUtil.notFound("Product or pending reservation not found."));
        } catch (IllegalArgumentException e) {
            ApiResponse<ProductResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}