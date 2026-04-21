package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductSearchCriteria;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.ActiveOrderException;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Category;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ReservationStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.StockReservationRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("ResultOfMethodCallIgnored")
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockReservationRepository stockReservationRepository;
    private final AuthIntegrationService authIntegrationService;

    @Override
    public Optional<ProductResponse> getProductById(UUID id) {
        return productRepository.findById(id)
                .filter(product -> product.getDeletedAt() == null && (product.getStatus() == ProductStatus.ACTIVE || product.getStatus() == ProductStatus.OUT_OF_STOCK))
                .filter(product -> {
                    Map<String, Object> profile = authIntegrationService.getJastiperProfile(product.getJastiperId());
                    if (profile != null && profile.containsKey("status")) {
                        return !"BANNED".equalsIgnoreCase((String) profile.get("status"));
                    }
                    return true;
                })
                .map(this::enrichProductResponse);
    }

    @Override
    public ProductResponse createProduct(UUID jastiperId, ProductCreateRequest req) {
        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId()).orElse(null);
            if (category != null) {
                category.setProductCount(category.getProductCount() + 1);
                categoryRepository.save(category);
            }
        }

        Product product = Product.builder()
                .jastiperId(jastiperId)
                .categoryId(category != null ? category.getCategoryId() : null)
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice() != null ? req.getPrice().intValue() : 0)
                .serviceFee(req.getServiceFee() != null ? req.getServiceFee().intValue() : 0)
                .stock(req.getStock())
                .originCountry(req.getOriginCountry())
                .purchaseDate(req.getPurchaseDate())
                .weightGram(req.getWeightGram())
                .images(req.getImages() != null ? req.getImages() : new ArrayList<>())
                .tags(req.getTags() != null ? req.getTags() : new ArrayList<>())
                .status(req.getStock() > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK)
                .build();

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Override
    public boolean deleteProduct(UUID jastiperId, UUID id) {
        Product existing = productRepository.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!existing.getJastiperId().equals(jastiperId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this product.");
        }

        long activeOrders = stockReservationRepository.countByProductProductIdAndStatusIn(id, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        if (activeOrders > 0) {
            throw new ActiveOrderException(activeOrders);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setStatus(ProductStatus.HIDDEN);

        if (existing.getCategoryId() != null) {
            categoryRepository.findById(existing.getCategoryId()).ifPresent(cat -> {
                cat.setProductCount(Math.max(0, cat.getProductCount() - 1));
                categoryRepository.save(cat);
            });
        }
        productRepository.save(existing);
        return true;
    }

    private ProductResponse mapToThumbnailResponse(Product p) {
        if (p.getTags() != null) p.getTags().size();
        if (p.getImages() != null) p.getImages().size();

        ProductResponse res = ProductResponse.fromEntity(p);
        if (res.getImages() != null && !res.getImages().isEmpty()) {
            res.setImages(List.of(res.getImages().getFirst()));
        }
        return res;
    }

    private ProductResponse enrichProductResponse(Product p) {
        if (p.getTags() != null) p.getTags().size();
        if (p.getImages() != null) p.getImages().size();

        ProductResponse res = ProductResponse.fromEntity(p);
        if (p.getCategoryId() != null) {
            categoryRepository.findById(p.getCategoryId()).ifPresent(cat -> {
                if (res.getCategory() == null) {
                    res.setCategory(ProductResponse.CategoryInfo.builder().id(p.getCategoryId()).build());
                }
                res.getCategory().setName(cat.getName());
            });
        }

        if (res.getJastiper() == null) {
            res.setJastiper(ProductResponse.JastiperInfo.builder().userId(p.getJastiperId()).build());
        }

        try {
            Map<String, Object> jastiperProfile = authIntegrationService.getJastiperProfile(p.getJastiperId());
            if (jastiperProfile != null && !jastiperProfile.isEmpty()) {
                res.getJastiper().setUsername((String) jastiperProfile.get("username"));
                res.getJastiper().setFullName((String) jastiperProfile.get("full_name"));
                res.getJastiper().setProfilePictureUrl((String) jastiperProfile.get("profile_picture_url"));

                if (jastiperProfile.containsKey("avg_rating")) {
                    res.getJastiper().setAvgRating(Double.valueOf(jastiperProfile.get("avg_rating").toString()));
                }
            }
        } catch (Exception ignored) {
            // Ignored
        }
        return res;
    }

    @Override
    public Optional<ProductResponse> updateProduct(UUID jastiperId, UUID id, ProductUpdateRequest req) {
        return productRepository.findByIdForUpdate(id).map(existing -> {
            if (!existing.getJastiperId().equals(jastiperId)) {
                throw new UnauthorizedAccessException("You are not authorized to modify this product.");
            }

            updateBasicFields(existing, req);
            updateStockAndStatus(existing, req);
            updateCategory(existing, req);

            Product savedProduct = productRepository.save(existing);
            if (savedProduct.getImages() != null) savedProduct.getImages().size();
            if (savedProduct.getTags() != null) savedProduct.getTags().size();
            return ProductResponse.fromEntity(savedProduct);
        });
    }

    private void updateBasicFields(Product existing, ProductUpdateRequest req) {
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        if (req.getPrice() != null) existing.setPrice(req.getPrice().intValue());
        if (req.getOriginCountry() != null) existing.setOriginCountry(req.getOriginCountry());
        if (req.getPurchaseDate() != null) existing.setPurchaseDate(req.getPurchaseDate());
        if (req.getServiceFee() != null) existing.setServiceFee(req.getServiceFee().intValue());
        if (req.getWeightGram() != null) existing.setWeightGram(req.getWeightGram());
        if (req.getImages() != null) existing.setImages(req.getImages());
        if (req.getTags() != null) existing.setTags(req.getTags());
    }

    private void updateStockAndStatus(Product existing, ProductUpdateRequest req) {
        if (req.getStock() != null) {
            existing.setStock(req.getStock());
            if (existing.getStock() <= 0) {
                existing.setStatus(ProductStatus.OUT_OF_STOCK);
            } else if (existing.getStatus() == ProductStatus.OUT_OF_STOCK) {
                existing.setStatus(ProductStatus.ACTIVE);
            }
        }
        if (req.getStatus() != null) {
            existing.setStatus(ProductStatus.valueOf(req.getStatus().toUpperCase()));
        }
    }

    private void updateCategory(Product existing, ProductUpdateRequest req) {
        if (req.getCategoryId() != null) {
            Integer oldCategoryId = existing.getCategoryId();
            Category newCategory = categoryRepository.findById(req.getCategoryId()).orElse(null);

            if (newCategory != null && !newCategory.getCategoryId().equals(oldCategoryId)) {
                if (oldCategoryId != null) {
                    categoryRepository.findById(oldCategoryId).ifPresent(oldCat -> {
                        oldCat.setProductCount(Math.max(0, oldCat.getProductCount() - 1));
                        categoryRepository.save(oldCat);
                    });
                }
                newCategory.setProductCount(newCategory.getProductCount() + 1);
                categoryRepository.save(newCategory);
                existing.setCategoryId(newCategory.getCategoryId());
            }
        }
    }

    @Override
    public Page<ProductResponse> searchProductsPublic(ProductSearchCriteria criteria, Pageable pageable) {
        criteria.setStatus(ProductStatus.ACTIVE);
        Specification<Product> spec = ProductSpecification.searchProducts(criteria);
        return productRepository.findAll(spec, pageable).map(this::mapToThumbnailResponse);
    }

    @Override
    public Page<ProductResponse> getMyCatalog(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.searchProducts(criteria);
        return productRepository.findAll(spec, pageable).map(this::mapToThumbnailResponse);
    }
}