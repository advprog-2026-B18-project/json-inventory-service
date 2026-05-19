package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.AdminProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AdminProductService {
    Page<ProductResponse> getAllProductsAdmin(String keyword, UUID jastiperId, String status, Integer categoryId, Pageable pageable);
    Optional<ProductResponse> moderateProduct(UUID adminId, UUID productId, AdminProductUpdateRequest req);
    Optional<ProductResponse> getAdminProductDetail(UUID productId);
}