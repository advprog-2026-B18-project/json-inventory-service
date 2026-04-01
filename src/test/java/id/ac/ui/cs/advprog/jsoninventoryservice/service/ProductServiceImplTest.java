package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product dummyProduct;
    private UUID jastiperId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        jastiperId = UUID.randomUUID();
        productId = UUID.randomUUID();
        dummyProduct = Product.builder()
                .id(productId)
                .jastiperId(jastiperId)
                .name("Test Product")
                .description("Desc")
                .price(100L)
                .stock(10)
                .originCountry("ID")
                .purchaseDate(LocalDate.now())
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void testCreateProduct() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("New Prod");
        req.setPrice(50L);
        req.setStock(5);
        req.setOriginCountry("JP");
        req.setPurchaseDate(LocalDate.now());

        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        ProductResponse response = productService.createProduct(jastiperId, req);

        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("Updated Name");
        req.setStock(20);

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        Optional<ProductResponse> response = productService.updateProduct(jastiperId, productId, req);

        assertTrue(response.isPresent());
        assertEquals("Updated Name", dummyProduct.getName());
        assertEquals(20, dummyProduct.getStock());
    }

    @Test
    void testUpdateProduct_FailWrongOwner() {
        UUID wrongJastiperId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.updateProduct(wrongJastiperId, productId, new ProductUpdateRequest());

        assertFalse(response.isPresent());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        boolean result = productService.deleteProduct(jastiperId, productId);

        assertTrue(result);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void testDeleteProduct_FailWrongOwner() {
        UUID wrongJastiperId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        boolean result = productService.deleteProduct(wrongJastiperId, productId);

        assertFalse(result);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void testReserveStock_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        Optional<ProductResponse> response = productService.reserveStock(productId, 2);

        assertTrue(response.isPresent());
        assertEquals(8, dummyProduct.getStock());
    }

    @Test
    void testReserveStock_FailInsufficientStock() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.reserveStock(productId, 20);

        assertFalse(response.isPresent());
        assertEquals(10, dummyProduct.getStock());
    }

    @Test
    void testGetProductById_Found() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.getProductById(productId);

        assertTrue(response.isPresent());
        assertEquals("Test Product", response.get().getName());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        Optional<ProductResponse> response = productService.getProductById(UUID.randomUUID());

        assertFalse(response.isPresent());
    }

    @Test
    void testReserveStock_BecomeOutOfStock() {
        dummyProduct.setStock(5);
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        Optional<ProductResponse> response = productService.reserveStock(productId, 5);

        assertTrue(response.isPresent());
        assertEquals(0, dummyProduct.getStock());
        assertEquals(ProductStatus.OUT_OF_STOCK.name(), response.get().getStatus());
    }

    @Test
    void testUpdateProduct_AllFields() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("New Name");
        req.setDescription("New Desc");
        req.setPrice(99L);
        req.setStock(50);
        req.setStatus("OUT_OF_STOCK");

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        Optional<ProductResponse> res = productService.updateProduct(jastiperId, productId, req);

        assertTrue(res.isPresent());
        assertEquals("New Name", dummyProduct.getName());
        assertEquals("New Desc", dummyProduct.getDescription());
        assertEquals(99L, dummyProduct.getPrice());
        assertEquals(50, dummyProduct.getStock());
        assertEquals(ProductStatus.OUT_OF_STOCK, dummyProduct.getStatus());
    }

    @Test
    void testMapToResponse_NullCollections() {
        dummyProduct.setImages(null);
        dummyProduct.setTags(null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> res = productService.getProductById(productId);

        assertTrue(res.isPresent());
        assertNotNull(res.get().getImages());
        assertNotNull(res.get().getTags());
    }

    @Test
    void testUpdateProduct_OwnerMismatchReturnsEmpty() {
        UUID wrongOwner = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.updateProduct(wrongOwner, productId, new ProductUpdateRequest());

        assertTrue(response.isEmpty());
    }

    @Test
    void testUpdateProduct_PartialFields() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setDescription("New Description");
        req.setStatus("HIDDEN");

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals("New Description", dummyProduct.getDescription());
        assertEquals(ProductStatus.HIDDEN, dummyProduct.getStatus());
        assertEquals("Test Product", dummyProduct.getName());
    }

    @Test
    void testReserveStock_StockBecomesZero() {
        dummyProduct.setStock(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        Optional<ProductResponse> response = productService.reserveStock(productId, 10);

        assertTrue(response.isPresent());
        assertEquals(0, dummyProduct.getStock());
        assertEquals(ProductStatus.OUT_OF_STOCK.name(), response.get().getStatus());
    }

    @Test
    void testMapToResponse_WithNullImagesAndTags() {
        dummyProduct.setImages(null);
        dummyProduct.setTags(null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.getProductById(productId);

        assertTrue(response.isPresent());
        assertNotNull(response.get().getImages());
        assertTrue(response.get().getImages().isEmpty());
    }

    @Test
    void testGetMyCatalog() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Product> productPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dummyProduct));

        when(productRepository
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(productPage);

        var result = productService.getMyCatalog(jastiperId, "Test", "ACTIVE", pageable);

        assertNotNull(result);
        verify(((org.springframework.data.jpa.repository.JpaSpecificationExecutor<Product>) productRepository), times(1))
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    void testSearchProductsPublic() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Product> productPage = new org.springframework.data.domain.PageImpl<>(List.of(dummyProduct));

        when(productRepository
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(productPage);

        var result = productService.searchProductsPublic("keyword", UUID.randomUUID(), 1000L, 50000L, pageable);
        assertNotNull(result);
        verify(((org.springframework.data.jpa.repository.JpaSpecificationExecutor<Product>) productRepository), times(1))
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    void testGetMyCatalog_WithNullFilters() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Product> productPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dummyProduct));

        when(productRepository
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(productPage);

        var result = productService.getMyCatalog(jastiperId, null, null, pageable);
        assertNotNull(result);
        verify(((org.springframework.data.jpa.repository.JpaSpecificationExecutor<Product>) productRepository), times(1))
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    void testGetMyCatalog_WithOnlySearchQuery() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Product> productPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dummyProduct));

        when(productRepository
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(productPage);

        var result = productService.getMyCatalog(jastiperId, "sepatu", null, pageable);
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void testGetMyCatalog_WithOnlyStatus() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Product> productPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dummyProduct));

        when(productRepository
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(productPage);

        var result = productService.getMyCatalog(jastiperId, null, "ACTIVE", pageable);
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void testGetMyCatalog_WithEmptyStrings() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Product> productPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dummyProduct));

        when(productRepository
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(productPage);

        var result = productService.getMyCatalog(jastiperId, "", "", pageable);
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }
}