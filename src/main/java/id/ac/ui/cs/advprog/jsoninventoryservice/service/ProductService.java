package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    Page<ProductResponse> searchProductsPublic(String keyword, UUID jastiperId, Long minPrice, Long maxPrice, Integer categoryId, Pageable pageable);    Page<ProductResponse> getMyCatalog(UUID jastiperId, String q, String status, Pageable pageable);
    Optional<ProductResponse> getProductById(UUID id);
    ProductResponse createProduct(UUID jastiperId, ProductCreateRequest request);
    Optional<ProductResponse> updateProduct(UUID jastiperId, UUID id, ProductUpdateRequest request);
    boolean deleteProduct(UUID jastiperId, UUID id);
}