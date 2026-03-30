package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    private ProductResponse mapToResponse(Product product) {

        List<String> safeImages = new ArrayList<>();
        if (product.getImages() != null) {
            safeImages.addAll(product.getImages());
        }

        List<String> safeTags = new ArrayList<>();
        if (product.getTags() != null) {
            safeTags.addAll(product.getTags());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .jastiperId(product.getJastiperId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .originCountry(product.getOriginCountry())
                .purchaseDate(product.getPurchaseDate())
                .images(safeImages)
                .tags(safeTags)
                .status(product.getStatus().name())
                .avgRating(product.getAvgRating())
                .totalOrders(product.getTotalOrders())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    public List<ProductResponse> getAllProductsPublic() {
        return productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getMyProducts(UUID jastiperId) {
        return productRepository.findAll().stream()
                .filter(p -> p.getJastiperId() != null && p.getJastiperId().equals(jastiperId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductResponse> getProductById(UUID id) {
        return productRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    public ProductResponse createProduct(UUID jastiperId, ProductCreateRequest req) {
        Product product = Product.builder()
                .jastiperId(jastiperId)
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .stock(req.getStock())
                .originCountry(req.getOriginCountry())
                .purchaseDate(req.getPurchaseDate())
                .status(ProductStatus.ACTIVE)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Override
    public Optional<ProductResponse> updateProduct(UUID jastiperId, UUID id, ProductUpdateRequest req) {
        return productRepository.findById(id).map(existing -> {
            if (!existing.getJastiperId().equals(jastiperId)) return null;

            if (req.getName() != null) existing.setName(req.getName());
            if (req.getDescription() != null) existing.setDescription(req.getDescription());
            if (req.getPrice() != null) existing.setPrice(req.getPrice());
            if (req.getStock() != null) existing.setStock(req.getStock());
            if (req.getStatus() != null) existing.setStatus(ProductStatus.valueOf(req.getStatus().toUpperCase()));

            return mapToResponse(productRepository.save(existing));
        });
    }

    @Override
    public boolean deleteProduct(UUID jastiperId, UUID id) {
        return productRepository.findById(id).map(existing -> {
            if (!existing.getJastiperId().equals(jastiperId)) return false;

            productRepository.deleteById(id);
            return true;
        }).orElse(false);
    }

    @Override
    public Optional<ProductResponse> reserveStock(UUID id, Integer quantity) {
        return productRepository.findById(id).map(p -> {
            if (p.getStock() >= quantity) {
                p.setStock(p.getStock() - quantity);
                if (p.getStock() == 0) p.setStatus(ProductStatus.OUT_OF_STOCK);

                return mapToResponse(productRepository.save(p));
            }
            return null;
        });
    }
}