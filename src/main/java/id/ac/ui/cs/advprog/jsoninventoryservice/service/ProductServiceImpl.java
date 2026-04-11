package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Category;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ReservationStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.StockReservationRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StockReservationRepository stockReservationRepository;

    @Override
    public Optional<ProductResponse> getProductById(UUID id) {
        return productRepository.findById(id).map(ProductResponse::fromEntity);
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
                .category(category)
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .serviceFee(req.getServiceFee() != null ? req.getServiceFee() : 0L)
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
    public Optional<ProductResponse> updateProduct(UUID jastiperId, UUID id, ProductUpdateRequest req) {
        return productRepository.findByIdForUpdate(id).map(existing -> {
            if (!existing.getJastiperId().equals(jastiperId)) return null;

            if (req.getName() != null) existing.setName(req.getName());
            if (req.getDescription() != null) existing.setDescription(req.getDescription());
            if (req.getPrice() != null) existing.setPrice(req.getPrice());
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

            if (req.getCategoryId() != null) {
                Category oldCategory = existing.getCategory();
                Category newCategory = categoryRepository.findById(req.getCategoryId()).orElse(null);
                if (newCategory != null && (oldCategory == null || !oldCategory.getCategoryId().equals(newCategory.getCategoryId()))) {
                    if (oldCategory != null) {
                        oldCategory.setProductCount(Math.max(0, oldCategory.getProductCount() - 1));
                        categoryRepository.save(oldCategory);
                    }
                    newCategory.setProductCount(newCategory.getProductCount() + 1);
                    categoryRepository.save(newCategory);
                    existing.setCategory(newCategory);
                }
            }

            return ProductResponse.fromEntity(productRepository.save(existing));
        });
    }

    @Override
    public boolean deleteProduct(UUID jastiperId, UUID id) {
        return productRepository.findByIdForUpdate(id).map(existing -> {
            if (!existing.getJastiperId().equals(jastiperId)) return false;

            long activeOrders = stockReservationRepository.countByProduct_IdAndStatus(id, ReservationStatus.PENDING);
            if (activeOrders > 0) {
                throw new IllegalStateException("Products that have active orders (PENDING status) cannot be deleted.");
            }

            existing.setDeletedAt(LocalDateTime.now());

            if (existing.getCategory() != null) {
                Category cat = existing.getCategory();
                cat.setProductCount(Math.max(0, cat.getProductCount() - 1));
                categoryRepository.save(cat);
            }

            productRepository.save(existing);
            return true;
        }).orElse(false);
    }

    @Override
    public Page<ProductResponse> searchProductsPublic(String keyword, UUID jastiperId, Long minPrice, Long maxPrice, Integer categoryId, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.searchProducts(keyword, jastiperId, minPrice, maxPrice, categoryId, ProductStatus.ACTIVE);

        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(ProductResponse::fromEntity);
    }

    @Override
    public Page<ProductResponse> getMyCatalog(UUID jastiperId, String q, String status, Pageable pageable) {
        ProductStatus filterStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            filterStatus = ProductStatus.valueOf(status.toUpperCase());
        }

        Specification<Product> spec = ProductSpecification.searchProducts(q, jastiperId, null, null, null, filterStatus);
        return productRepository.findAll(spec, pageable).map(ProductResponse::fromEntity);
    }
}