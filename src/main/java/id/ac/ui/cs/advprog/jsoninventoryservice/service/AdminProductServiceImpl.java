package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.AdminProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductSearchCriteria;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.event.ProductModeratedEvent;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.ModerationLog;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ModerationAction;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ModerationLogRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("ResultOfMethodCallIgnored")
public class AdminProductServiceImpl implements AdminProductService {
    private final ProductRepository productRepository;
    private final ModerationLogRepository moderationLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    private ProductResponse mapToResponse(Product p) {
        if (p.getImages() != null) p.getImages().size();
        if (p.getTags() != null) p.getTags().size();
        return ProductResponse.fromEntity(p);
    }

    @Override
    public Page<ProductResponse> getAllProductsAdmin(String keyword, UUID jastiperId, String status, Integer categoryId, Pageable pageable) {
        ProductStatus filterStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                filterStatus = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Ignored
            }
        }

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .jastiperId(jastiperId)
                .categoryId(categoryId)
                .status(filterStatus)
                .build();
        Specification<Product> spec = ProductSpecification.searchProducts(criteria);

        return productRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    public Optional<ProductResponse> getAdminProductDetail(UUID id) {
        return productRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public Optional<ProductResponse> moderateProduct(UUID adminId, UUID id, AdminProductUpdateRequest request) {
        return productRepository.findByIdForUpdate(id).map(product -> {
            ModerationAction action;
            try {
                action = ModerationAction.valueOf(request.getAction().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid moderation action. Allowed: REMOVE, RESTORE, HIDE, ACTIVATE");
            }

            switch (action) {
                case HIDE -> product.setStatus(ProductStatus.HIDDEN);
                case REMOVE -> {
                    product.setStatus(ProductStatus.REMOVED_BY_ADMIN);
                    product.setDeletedAt(LocalDateTime.now());
                }
                default -> {
                    product.setStatus(ProductStatus.ACTIVE);
                    product.setDeletedAt(null);
                }
            }
            productRepository.save(product);

            ModerationLog log = new ModerationLog();
            log.setProduct(product);
            log.setAdminId(adminId);
            log.setAction(action);
            log.setReason(request.getReason());
            moderationLogRepository.save(log);

            eventPublisher.publishEvent(new ProductModeratedEvent(product.getProductId(), adminId, action.name(), request.getReason(), product.getName(), product.getJastiperId()));
            return mapToResponse(product);
        });
    }
}