package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    List<ProductResponse> getAllProductsPublic();
    List<ProductResponse> getMyProducts(UUID jastiperId);
    Optional<ProductResponse> getProductById(UUID id);
    ProductResponse createProduct(UUID jastiperId, ProductCreateRequest request);
    Optional<ProductResponse> updateProduct(UUID jastiperId, UUID id, ProductUpdateRequest request);
    boolean deleteProduct(UUID jastiperId, UUID id);
    Optional<ProductResponse> reserveStock(UUID id, Integer quantity);
}