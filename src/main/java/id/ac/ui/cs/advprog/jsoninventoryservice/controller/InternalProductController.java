package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.PostOrderRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReleaseRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReserveRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.StockManagementService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final StockManagementService stockService;

    @PostMapping("/{id}/stock/reserve")
    public ResponseEntity<ApiResponse<ProductResponse>> reserveStock(@PathVariable UUID id, @RequestBody StockReserveRequest request) {
        return stockService.reserveStock(id, request)
                .map(p -> ResponseUtil.success(p, "Stock reserved successfully."))
                .orElseGet(() -> {
                    ApiResponse<ProductResponse> errorResponse = new ApiResponse<>();
                    errorResponse.setSuccess(false);
                    errorResponse.setMessage("Product not found, unavailable, or insufficient stock.");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
                });
    }

    @PostMapping("/{id}/stock/release")
    public ResponseEntity<ApiResponse<ProductResponse>> releaseStock(@PathVariable UUID id, @RequestBody StockReleaseRequest request) {
        return stockService.releaseStock(id, request)
                .map(p -> ResponseUtil.success(p, "Stock released successfully."))
                .orElse(ResponseUtil.notFound("Reservation not found or already released."));
    }

    @PostMapping("/{id}/post-order")
    public ResponseEntity<ApiResponse<ProductResponse>> processPostOrder(
            @PathVariable UUID id,
            @RequestBody PostOrderRequest request) {

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