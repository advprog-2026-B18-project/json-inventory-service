package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.PostOrderRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReleaseRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReserveRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;

import java.util.Optional;
import java.util.UUID;

public interface StockManagementService {
    Optional<ProductResponse> reserveStock(UUID id, StockReserveRequest request);
    Optional<ProductResponse> releaseStock(UUID id, StockReleaseRequest request);
    void cleanupExpiredReservations();
    Optional<ProductResponse> processPostOrder(UUID id, PostOrderRequest request);
}