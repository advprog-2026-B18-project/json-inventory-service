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
@SuppressWarnings("unchecked")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

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
    void testCreateProduct_WithAllFieldsAndZeroStock() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("New Prod Complete");
        req.setDescription("Desc Complete");
        req.setPrice(50L);
        req.setStock(0);
        req.setOriginCountry("JP");
        req.setPurchaseDate(LocalDate.now());
        req.setCategoryId(1);
        req.setServiceFee(15000L);
        req.setWeightGram(500);
        req.setImages(List.of("image.jpg"));
        req.setTags(List.of("tag1"));

        Category dummyCategory = new Category();
        dummyCategory.setCategoryId(1);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(dummyCategory));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        ProductResponse response = productService.createProduct(jastiperId, req);

        assertNotNull(response);
        verify(categoryRepository, times(1)).findById(1);
    }

    @Test
    void testUpdateProduct_Success() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("Updated Name");
        req.setStock(20);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        Optional<ProductResponse> response = productService.updateProduct(jastiperId, productId, req);

        assertTrue(response.isPresent());
        assertEquals("Updated Name", dummyProduct.getName());
        assertEquals(20, dummyProduct.getStock());
    }

    @Test
    void testUpdateProduct_FailWrongOwner() {
        UUID wrongJastiperId = UUID.randomUUID();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.updateProduct(wrongJastiperId, productId, new ProductUpdateRequest());

        assertFalse(response.isPresent());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_FailWrongOwner() {
        UUID wrongJastiperId = UUID.randomUUID();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));

        boolean result = productService.deleteProduct(wrongJastiperId, productId);

        assertFalse(result);
        verify(productRepository, never()).save(any(Product.class));
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
    void testUpdateProduct_AllFields() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("New Name");
        req.setDescription("New Desc");
        req.setPrice(99L);
        req.setStock(50);
        req.setStatus("OUT_OF_STOCK");

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
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
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.updateProduct(wrongOwner, productId, new ProductUpdateRequest());

        assertTrue(response.isEmpty());
    }

    @Test
    void testUpdateProduct_PartialFields() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setDescription("New Description");
        req.setStatus("HIDDEN");

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals("New Description", dummyProduct.getDescription());
        assertEquals(ProductStatus.HIDDEN, dummyProduct.getStatus());
        assertEquals("Test Product", dummyProduct.getName());
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

        var result = productService.searchProductsPublic("keyword", UUID.randomUUID(), 1000L, 50000L, null, pageable);        assertNotNull(result);
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

    @Test
    void testUpdateProduct_EmptyRequest_DoesNotUpdateFields() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any())).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals("Test Product", dummyProduct.getName());
    }

    @Test
    void testUpdateProduct_CategoryNotFound() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setCategoryId(999);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());
        when(productRepository.save(any())).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);
        assertNotNull(dummyProduct);
    }

    @Test
    void testUpdateProduct_SwitchCategory_OldCategoryNull() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setCategoryId(2);

        Category newCat = new Category(); newCat.setCategoryId(2); newCat.setProductCount(0);
        dummyProduct.setCategory(null);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(newCat));
        when(productRepository.save(any())).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(1, newCat.getProductCount());
        assertEquals(newCat, dummyProduct.getCategory());
    }

    @Test
    void testUpdateProduct_SameCategory_DoesNothing() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setCategoryId(1);

        Category cat = new Category(); cat.setCategoryId(1); cat.setProductCount(5);
        dummyProduct.setCategory(cat);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        when(productRepository.save(any())).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(5, cat.getProductCount());
    }

    @Test
    void testUpdateProduct_SwitchCategory_OldCategoryNotNullAndDifferent() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setCategoryId(2);

        Category oldCat = new Category(); oldCat.setCategoryId(1); oldCat.setProductCount(5);
        Category newCat = new Category(); newCat.setCategoryId(2); newCat.setProductCount(10);

        dummyProduct.setCategory(oldCat);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(newCat));
        when(productRepository.save(any())).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(4, oldCat.getProductCount());
        assertEquals(11, newCat.getProductCount());
        assertEquals(newCat, dummyProduct.getCategory());
    }

    @Test
    void testCreateProduct_CategoryIdProvidedButNotFound() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("New Prod");
        req.setCategoryId(999);
        req.setStock(10);
        req.setPrice(50000L);

        when(categoryRepository.findById(999)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        ProductResponse response = productService.createProduct(jastiperId, req);

        assertNotNull(response);
        verify(categoryRepository, times(1)).findById(999);
    }

    @Test
    void testDeleteProduct_ThrowsExceptionWhenActiveOrdersExist() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(stockReservationRepository.countByProduct_IdAndStatus(productId, ReservationStatus.PENDING)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> productService.deleteProduct(jastiperId, productId));
    }

    @Test
    void testUpdateProduct_SetStockToZero_ChangesStatusToOutOfStock() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setStock(0);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(0, dummyProduct.getStock());
        assertEquals(ProductStatus.OUT_OF_STOCK, dummyProduct.getStatus());
    }

    @Test
    void testUpdateProduct_SetStockGreaterThanZero_ChangesStatusToActive() {
        dummyProduct.setStock(0);
        dummyProduct.setStatus(ProductStatus.OUT_OF_STOCK);

        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setStock(5);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(5, dummyProduct.getStock());
        assertEquals(ProductStatus.ACTIVE, dummyProduct.getStatus());
    }

    @Test
    void testUpdateProduct_CategoryIdNotFound_ButOldCategoryExists() {
        Category oldCat = new Category();
        oldCat.setCategoryId(1);
        dummyProduct.setCategory(oldCat);

        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setCategoryId(999);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(oldCat, dummyProduct.getCategory());
    }

    @Test
    void testUpdateProduct_SetStockNegative_Branch() {
        dummyProduct.setStatus(ProductStatus.OUT_OF_STOCK);
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setStock(-5);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(-5, dummyProduct.getStock());
    }

    @Test
    void testUpdateProduct_StockGreaterThanZero_StatusHidden_Branch() {
        dummyProduct.setStatus(ProductStatus.HIDDEN);
        dummyProduct.setStock(10);
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setStock(15);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(ProductStatus.HIDDEN, dummyProduct.getStatus());
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(stockReservationRepository.countByProduct_IdAndStatus(productId, ReservationStatus.PENDING)).thenReturn(0L);

        boolean result = productService.deleteProduct(jastiperId, productId);

        assertTrue(result);
        verify(productRepository, times(1)).save(any(Product.class));
        assertNotNull(dummyProduct.getDeletedAt());
    }

    @Test
    void testDeleteProduct_SuccessWithNullCategory() {
        dummyProduct.setCategory(null);
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(stockReservationRepository.countByProduct_IdAndStatus(productId, ReservationStatus.PENDING)).thenReturn(0L);

        boolean result = productService.deleteProduct(jastiperId, productId);

        assertTrue(result);
        assertNotNull(dummyProduct.getDeletedAt());
    }

    @Test
    void testDeleteProduct_WithCategory_UpdatesCategoryCount() {
        Category cat = new Category();
        cat.setCategoryId(1);
        cat.setProductCount(5);
        dummyProduct.setCategory(cat);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(stockReservationRepository.countByProduct_IdAndStatus(productId, ReservationStatus.PENDING)).thenReturn(0L);

        boolean result = productService.deleteProduct(jastiperId, productId);

        assertTrue(result);
        assertEquals(4, cat.getProductCount());
        assertNotNull(dummyProduct.getDeletedAt());
        verify(categoryRepository).save(cat);
    }

    @Test
    void testUpdateProduct_StockGreaterThanZero_StatusAlreadyActive_Branch() {
        dummyProduct.setStock(5);
        dummyProduct.setStatus(ProductStatus.ACTIVE);

        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setStock(15);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        productService.updateProduct(jastiperId, productId, req);

        assertEquals(15, dummyProduct.getStock());
        assertEquals(ProductStatus.ACTIVE, dummyProduct.getStatus());
    }
}