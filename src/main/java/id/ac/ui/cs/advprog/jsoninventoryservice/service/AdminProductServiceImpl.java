package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.AdminProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductSearchCriteria;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.event.ProductModeratedEvent;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.ModerationLog;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ModerationAction;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ModerationLogRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuthIntegrationService authIntegrationService;
    private final ModerationLogRepository moderationLogRepository;
    private final ApplicationEventPublisher eventPublisher;

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
                .includeDeleted(true)
                .build();
        Specification<Product> spec = ProductSpecification.searchProducts(criteria);

        return productRepository.findAll(spec, pageable).map(this::enrichProductResponse);
    }

    @Override
    public Optional<ProductResponse> getAdminProductDetail(UUID id) {
        return productRepository.findById(id).map(this::enrichProductResponse);
    }

    @Override
    @Transactional
    public Optional<ProductResponse> moderateProduct(UUID adminId, UUID id, AdminProductUpdateRequest request) {
        return productRepository.findByIdForUpdate(id).map(product -> {
            ProductStatus oldStatus = product.getStatus();

            ModerationAction action = parseModerationAction(request.getAction());
            applyModerationAction(product, action);

            ProductStatus newStatus = product.getStatus();

            updateCategoryProductCount(product.getCategoryId(), oldStatus, newStatus);

            productRepository.save(product);
            logAndPublishModeration(product, adminId, action, request.getReason());
            return enrichProductResponse(product);
        });
    }

    private void updateCategoryProductCount(Integer categoryId, ProductStatus oldStatus, ProductStatus newStatus) {
        if (categoryId == null) return;

        boolean wasActive = (oldStatus == ProductStatus.ACTIVE || oldStatus == ProductStatus.OUT_OF_STOCK);
        boolean isActiveNow = (newStatus == ProductStatus.ACTIVE || newStatus == ProductStatus.OUT_OF_STOCK);

        if (wasActive && !isActiveNow) {
            categoryRepository.findById(categoryId).ifPresent(cat -> {
                cat.setProductCount(Math.max(0, cat.getProductCount() - 1));
                categoryRepository.save(cat);
            });
        }
        else if (!wasActive && isActiveNow) {
            categoryRepository.findById(categoryId).ifPresent(cat -> {
                cat.setProductCount(cat.getProductCount() + 1);
                categoryRepository.save(cat);
            });
        }
    }

    private ModerationAction parseModerationAction(String actionStr) {
        try {
            return ModerationAction.valueOf(actionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid moderation action. Allowed: REMOVE, RESTORE, HIDE, ACTIVATE");
        }
    }

    private void applyModerationAction(Product product, ModerationAction action) {
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
    }

    private void logAndPublishModeration(Product product, UUID adminId, ModerationAction action, String reason) {
        ModerationLog log = new ModerationLog();
        log.setProduct(product);
        log.setAdminId(adminId);
        log.setAction(action);
        log.setReason(reason);
        moderationLogRepository.save(log);

        eventPublisher.publishEvent(new ProductModeratedEvent(
                product.getProductId(), adminId, action.name(),
                reason, product.getName(), product.getJastiperId()));
    }

    private ProductResponse enrichProductResponse(Product entity) {
        if (entity.getImages() != null) Hibernate.initialize(entity.getImages());
        if (entity.getTags() != null) Hibernate.initialize(entity.getTags());

        ProductResponse responseDto = ProductResponse.fromEntity(entity);
        Integer entityCategoryId = entity.getCategoryId();
        UUID entityJastiperId = entity.getJastiperId();

        if (entityCategoryId != null) {
            categoryRepository.findById(entityCategoryId).ifPresent(foundCategory -> {
                if (responseDto.getCategory() == null) {
                    responseDto.setCategory(ProductResponse.CategoryInfo.builder().id(entityCategoryId).build());
                }
                responseDto.getCategory().setName(foundCategory.getName());
            });
        }

        if (responseDto.getJastiper() == null) {
            responseDto.setJastiper(ProductResponse.JastiperInfo.builder().userId(entityJastiperId).build());
        }

        try {
            Map<String, Object> profileData = authIntegrationService.getJastiperProfile(entityJastiperId);
            if (profileData != null && !profileData.isEmpty()) {
                responseDto.getJastiper().setUsername((String) profileData.get("username"));
                responseDto.getJastiper().setFullName((String) profileData.get("full_name"));
                responseDto.getJastiper().setProfilePictureUrl((String) profileData.get("profile_picture_url"));

                Object ratingVal = profileData.containsKey("avg_rating")
                        ? profileData.get("avg_rating")
                        : profileData.get("rating");
                if (ratingVal != null) {
                    responseDto.getJastiper().setAvgRating(Double.valueOf(ratingVal.toString()));
                }

                if (profileData.get("stats") instanceof Map<?, ?> stats && stats.containsKey("total_orders")) {
                    responseDto.getJastiper().setTotalOrders(((Number) stats.get("total_orders")).intValue());
                }
            }
        } catch (Exception ignored) {
            // Ignored
        }

        return responseDto;
    }
}