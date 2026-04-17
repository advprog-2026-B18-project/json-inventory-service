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
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthIntegrationService authIntegrationService;

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
                .productId(productId)
                .jastiperId(jastiperId)
                .name("Test Product")
                .description("Desc")
                .price(100)
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
        dummyCategory.setProductCount(0);
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
        ProductUpdateRequest request = new ProductUpdateRequest();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        assertThrows(UnauthorizedAccessException.class, () -> productService.updateProduct(wrongJastiperId, productId, request));
    }

    @Test
    void testDeleteProduct_FailWrongOwner() {
        UUID wrongJastiperId = UUID.randomUUID();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        assertThrows(UnauthorizedAccessException.class, () -> productService.deleteProduct(wrongJastiperId, productId));
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
        assertEquals(99, dummyProduct.getPrice());
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
        assertNull(res.get().getImages());
        assertNull(res.get().getTags());
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
        assertNull(response.get().getImages());
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
        Category newCat = new Category();
        newCat.setCategoryId(2);
        newCat.setProductCount(0);
        dummyProduct.setCategoryId(null);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(newCat));
        when(productRepository.save(any())).thenReturn(dummyProduct);
        productService.updateProduct(jastiperId, productId, req);
        assertEquals(1, newCat.getProductCount());
        assertEquals(2, dummyProduct.getCategoryId());
    }

    @Test
    void testUpdateProduct_SameCategory_DoesNothing() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setCategoryId(1);
        Category cat = new Category();
        cat.setCategoryId(1);
        cat.setProductCount(5);
        dummyProduct.setCategoryId(1);

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
        Category oldCat = new Category();
        oldCat.setCategoryId(1);
        oldCat.setProductCount(5);
        Category newCat = new Category();
        newCat.setCategoryId(2);
        newCat.setProductCount(10);
        dummyProduct.setCategoryId(1);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(oldCat));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(newCat));
        when(productRepository.save(any())).thenReturn(dummyProduct);
        productService.updateProduct(jastiperId, productId, req);

        assertEquals(4, oldCat.getProductCount());
        assertEquals(11, newCat.getProductCount());
        assertEquals(2, dummyProduct.getCategoryId());
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
        dummyProduct.setCategoryId(1);
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setCategoryId(999);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);
        productService.updateProduct(jastiperId, productId, req);
        assertEquals(1, dummyProduct.getCategoryId());
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

    @Test
    void testGetProductById_HiddenProduct_ReturnsEmpty() {
        productId = UUID.randomUUID();
        Product hiddenProduct = new Product();
        hiddenProduct.setProductId(productId);
        hiddenProduct.setStatus(ProductStatus.HIDDEN);

        when(productRepository.findById(productId)).thenReturn(Optional.of(hiddenProduct));
        Optional<ProductResponse> result = productService.getProductById(productId);
        assertTrue(result.isEmpty(), "The public may not see products with HIDDEN status");
    }

    @Test
    void testGetProductById_SoftDeletedProduct_ReturnsEmpty() {
        productId = UUID.randomUUID();
        Product deletedProduct = new Product();
        deletedProduct.setProductId(productId);
        deletedProduct.setStatus(ProductStatus.ACTIVE);
        deletedProduct.setDeletedAt(LocalDateTime.now());

        when(productRepository.findById(productId)).thenReturn(Optional.of(deletedProduct));
        Optional<ProductResponse> result = productService.getProductById(productId);
        assertTrue(result.isEmpty(), "The public should not see products that have been removed");
    }

    @Test
    void testGetProductById_OutOfStockProduct_ReturnsProduct() {
        productId = UUID.randomUUID();
        Product outProduct = new Product();
        outProduct.setProductId(productId);
        outProduct.setStatus(ProductStatus.OUT_OF_STOCK);
        outProduct.setPrice(100000);
        outProduct.setStock(0);

        when(productRepository.findById(productId)).thenReturn(Optional.of(outProduct));
        Optional<ProductResponse> result = productService.getProductById(productId);
        assertTrue(result.isPresent());
    }

    @Test
    void testCreateProduct_WithNullPriceAndServiceFee() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("Product Name");
        req.setPrice(null);
        req.setServiceFee(null);
        req.setStock(5);
        req.setOriginCountry("Indonesia");
        req.setPurchaseDate(LocalDate.now());

        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);
        ProductResponse response = productService.createProduct(jastiperId, req);
        assertNotNull(response);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_ThrowsExceptionWhenActiveOrdersExist() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(stockReservationRepository.countByProductProductIdAndStatusIn(eq(productId), anyList())).thenReturn(1L);
        assertThrows(ActiveOrderException.class, () -> productService.deleteProduct(jastiperId, productId));
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(stockReservationRepository.countByProductProductIdAndStatusIn(eq(productId), anyList())).thenReturn(0L);
        boolean result = productService.deleteProduct(jastiperId, productId);
        assertTrue(result);
        verify(productRepository, times(1)).save(any(Product.class));
        assertNotNull(dummyProduct.getDeletedAt());
    }

    @Test
    void testDeleteProduct_SuccessWithNullCategory() {
        dummyProduct.setCategoryId(null);
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(stockReservationRepository.countByProductProductIdAndStatusIn(eq(productId), anyList())).thenReturn(0L);
        boolean result = productService.deleteProduct(jastiperId, productId);
        assertTrue(result);
        assertNotNull(dummyProduct.getDeletedAt());
    }

    @Test
    void testDeleteProduct_WithCategory_UpdatesCategoryCount() {
        Category cat = new Category();
        cat.setCategoryId(1);
        cat.setProductCount(5);
        dummyProduct.setCategoryId(1);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        when(stockReservationRepository.countByProductProductIdAndStatusIn(eq(productId), anyList())).thenReturn(0L);

        boolean result = productService.deleteProduct(jastiperId, productId);
        assertTrue(result);
        assertEquals(4, cat.getProductCount());
        assertNotNull(dummyProduct.getDeletedAt());
        verify(categoryRepository).save(cat);
    }

    @Test
    void testDeleteProduct_FailNotFound() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> productService.deleteProduct(jastiperId, productId));
    }

    @Test
    void testUpdateProduct_WithPhase4Parameters() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setOriginCountry("US");
        req.setPurchaseDate(LocalDate.now());
        req.setServiceFee(5000L);
        req.setWeightGram(100);
        req.setImages(List.of("img1.jpg"));
        req.setTags(List.of("tag1"));

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);
        Optional<ProductResponse> res = productService.updateProduct(jastiperId, productId, req);
        assertTrue(res.isPresent());
        assertEquals("US", dummyProduct.getOriginCountry());
        assertEquals(5000, dummyProduct.getServiceFee());
    }

    @Test
    void testGetProductById_Success_WithEnrichment() {
        productId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        Product product = new Product();
        product.setProductId(productId);
        product.setJastiperId(jastiperId);
        product.setCategoryId(1);
        product.setStatus(ProductStatus.ACTIVE);
        product.setPrice(100000);
        product.setStock(10);
        Category category = new Category();
        category.setCategoryId(1);
        category.setName("Electronics");
        Map<String, Object> mockProfile = new HashMap<>();
        mockProfile.put("username", "jastiper123");
        mockProfile.put("status", "ACTIVE");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(authIntegrationService.getJastiperProfile(jastiperId)).thenReturn(mockProfile);

        Optional<ProductResponse> result = productService.getProductById(productId);
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getCategory().getName());
        assertEquals("jastiper123", result.get().getJastiper().getUsername());
    }

    @Test
    void testGetProductById_JastiperBanned_ReturnsEmpty() {
        productId = UUID.randomUUID();
        Product product = new Product();
        product.setProductId(productId);
        product.setJastiperId(UUID.randomUUID());
        product.setStatus(ProductStatus.ACTIVE);
        Map<String, Object> bannedProfile = new HashMap<>();
        bannedProfile.put("status", "BANNED");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(authIntegrationService.getJastiperProfile(product.getJastiperId())).thenReturn(bannedProfile);

        Optional<ProductResponse> result = productService.getProductById(productId);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetProductById_ProfileExistsButMissingAvgRating() {
        dummyProduct.setPrice(100);
        dummyProduct.setStock(10);
        dummyProduct.setCategoryId(1);
        Category c = new Category();
        c.setName("Category Test");
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", "testuser");
        profile.put("status", "ACTIVE");

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(c));
        when(authIntegrationService.getJastiperProfile(jastiperId)).thenReturn(profile);

        Optional<ProductResponse> res = productService.getProductById(productId);
        assertTrue(res.isPresent());
        assertNull(res.get().getJastiper().getAvgRating());
    }

    @Test
    void testGetProductById_ProfileMissingStatusKey() {
        dummyProduct.setPrice(100);
        dummyProduct.setStock(10);
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", "testuser");

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(authIntegrationService.getJastiperProfile(jastiperId)).thenReturn(profile);
        Optional<ProductResponse> res = productService.getProductById(productId);
        assertTrue(res.isPresent());
    }

    @Test
    void testGetProductById_CategoryNull() {
        dummyProduct.setPrice(100);
        dummyProduct.setStock(10);
        dummyProduct.setCategoryId(null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(authIntegrationService.getJastiperProfile(jastiperId)).thenReturn(null);
        Optional<ProductResponse> res = productService.getProductById(productId);
        assertTrue(res.isPresent());
        assertNull(res.get().getCategory());
    }

    @Test
    void testGetProductById_ProfileContainsTotalOrders() {
        dummyProduct.setCategoryId(1);
        dummyProduct.setPrice(100);
        dummyProduct.setStock(10);
        Category c = new Category();
        c.setName("Test Category");

        Map<String, Object> profile = new HashMap<>();
        profile.put("username", "testuser");
        profile.put("full_name", "Test User");
        profile.put("profile_picture_url", "https://image.com");
        profile.put("avg_rating", 4.5);
        profile.put("total_orders", 20);

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(c));
        when(authIntegrationService.getJastiperProfile(jastiperId)).thenReturn(profile);

        Optional<ProductResponse> res = productService.getProductById(productId);
        assertTrue(res.isPresent());
        assertNotNull(res.get().getJastiper());
    }

    @Test
    void testGetProductById_AuthServiceThrowsException() {
        dummyProduct.setCategoryId(1);
        dummyProduct.setPrice(100);
        dummyProduct.setStock(10);
        Category c = new Category();
        c.setName("Test Category");

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(authIntegrationService.getJastiperProfile(jastiperId)).thenThrow(new RuntimeException("Auth Service Down"));

        try {
            Optional<ProductResponse> res = productService.getProductById(productId);
            assertTrue(res.isPresent());
            assertNotNull(res.get().getJastiper());
        } catch (RuntimeException e) {
            assertEquals("Auth Service Down", e.getMessage());
        }
    }

    @Test
    void testGetProductById_AuthServiceReturnsNull() {
        dummyProduct.setCategoryId(1);
        dummyProduct.setPrice(100);
        dummyProduct.setStock(10);
        Category c = new Category();
        c.setName("Test Category");

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(c));
        when(authIntegrationService.getJastiperProfile(jastiperId)).thenReturn(null);

        Optional<ProductResponse> res = productService.getProductById(productId);
        assertTrue(res.isPresent());
    }

    @Test
    void testGetProductById_CategoryNullSafeBranch() {
        Product spyProduct = spy(new Product());
        spyProduct.setProductId(productId);
        spyProduct.setJastiperId(jastiperId);
        spyProduct.setStatus(ProductStatus.ACTIVE);
        spyProduct.setPrice(100);
        spyProduct.setStock(10);

        when(spyProduct.getCategoryId()).thenReturn(null, 1, 1);

        Category c = new Category();
        c.setName("Spy Category");

        when(productRepository.findById(productId)).thenReturn(Optional.of(spyProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(c));
        when(authIntegrationService.getJastiperProfile(jastiperId)).thenReturn(new HashMap<>());

        Optional<ProductResponse> res = productService.getProductById(productId);
        assertTrue(res.isPresent());
        assertEquals("Spy Category", res.get().getCategory().getName());
    }

    @Test
    void testGetProductById_AuthServiceThrowsException_CaughtGracefully() {
        UUID prodId = UUID.randomUUID();
        Product dummy = new Product();
        dummy.setProductId(prodId);
        dummy.setJastiperId(UUID.randomUUID());
        dummy.setStatus(ProductStatus.ACTIVE);
        dummy.setPrice(100000);
        dummy.setStock(10);

        when(productRepository.findById(prodId)).thenReturn(Optional.of(dummy));
        when(authIntegrationService.getJastiperProfile(any())).thenReturn(new HashMap<>()).thenThrow(new RuntimeException("Simulated Failure"));
        Optional<ProductResponse> res = productService.getProductById(prodId);
        assertTrue(res.isPresent());
    }

    @Test
    void testEnrichProductResponse_JastiperAndCategoryIsNull_Branch() {
        UUID prodId = UUID.randomUUID();
        Product dummy = new Product();
        dummy.setProductId(prodId);
        dummy.setJastiperId(UUID.randomUUID());
        dummy.setStatus(ProductStatus.ACTIVE);
        dummy.setPrice(100);
        dummy.setStock(10);
        ProductResponse mockResponse = new ProductResponse();
        mockResponse.setJastiper(null);
        mockResponse.setCategory(null);

        when(productRepository.findById(prodId)).thenReturn(Optional.of(dummy));
        when(authIntegrationService.getJastiperProfile(any())).thenReturn(new HashMap<>());
        try (MockedStatic<ProductResponse> mockedStatic = mockStatic(ProductResponse.class)) {
            mockedStatic.when(() -> ProductResponse.fromEntity(dummy)).thenReturn(mockResponse);
            Optional<ProductResponse> res = productService.getProductById(prodId);
            assertTrue(res.isPresent());
            assertNotNull(res.get().getJastiper());
        }
    }

    @Test
    void testGetMyCatalog() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(dummyProduct));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        ProductSearchCriteria criteria = ProductSearchCriteria.builder().jastiperId(jastiperId).keyword("Test").status(ProductStatus.ACTIVE).build();
        var result = productService.getMyCatalog(criteria, pageable);
        assertNotNull(result);
    }

    @Test
    void testSearchProductsPublic() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(dummyProduct));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        ProductSearchCriteria criteria = ProductSearchCriteria.builder().keyword("keyword").jastiperId(UUID.randomUUID()).minPrice(1000L).maxPrice(50000L).build();
        var result = productService.searchProductsPublic(criteria, pageable);
        assertNotNull(result);
    }

    @Test
    void testGetMyCatalog_WithNullFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(dummyProduct));
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

        ProductSearchCriteria criteria = ProductSearchCriteria.builder().jastiperId(jastiperId).build();
        var result = productService.getMyCatalog(criteria, pageable);
        assertNotNull(result);
    }

    @Test
    void testGetMyCatalog_WithOnlySearchQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(dummyProduct));
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

        ProductSearchCriteria criteria = ProductSearchCriteria.builder().jastiperId(jastiperId).keyword("shoes").build();
        var result = productService.getMyCatalog(criteria, pageable);
        assertNotNull(result);
    }

    @Test
    void testGetMyCatalog_WithOnlyStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(dummyProduct));
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

        ProductSearchCriteria criteria = ProductSearchCriteria.builder().jastiperId(jastiperId).status(ProductStatus.ACTIVE).build();
        var result = productService.getMyCatalog(criteria, pageable);
        assertNotNull(result);
    }

    @Test
    void testGetMyCatalog_WithEmptyStrings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(dummyProduct));
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

        ProductSearchCriteria criteria = ProductSearchCriteria.builder().jastiperId(jastiperId).keyword("").build();
        var result = productService.getMyCatalog(criteria, pageable);
        assertNotNull(result);
    }

    @Test
    void testGetMyCatalog_WithEmptyImages() {
        Pageable pageable = PageRequest.of(0, 10);
        Product emptyImageProduct = new Product();
        emptyImageProduct.setProductId(UUID.randomUUID());
        emptyImageProduct.setImages(new ArrayList<>());
        emptyImageProduct.setPrice(100000);
        emptyImageProduct.setStock(5);
        Page<Product> productPage = new PageImpl<>(List.of(emptyImageProduct));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        ProductSearchCriteria criteria = ProductSearchCriteria.builder().jastiperId(jastiperId).build();

        var result = productService.getMyCatalog(criteria, pageable);
        assertNotNull(result);
        assertTrue(result.getContent().getFirst().getImages().isEmpty());
    }

    @Test
    void testGetMyCatalog_WithNullImages_Branch() {
        Pageable pageable = PageRequest.of(0, 10);
        Product nullImageProduct = new Product();
        nullImageProduct.setProductId(UUID.randomUUID());
        nullImageProduct.setPrice(50000);
        nullImageProduct.setStock(10);
        nullImageProduct.setImages(null);
        Page<Product> productPage = new PageImpl<>(List.of(nullImageProduct));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        ProductSearchCriteria criteria = ProductSearchCriteria.builder().jastiperId(jastiperId).build();

        var result = productService.getMyCatalog(criteria, pageable);
        assertNotNull(result);
        assertNull(result.getContent().getFirst().getImages());
    }

    @Test
    void testGetMyCatalog_WithPopulatedImages_Branch() {
        Pageable pageable = PageRequest.of(0, 10);
        Product populatedImageProduct = new Product();
        populatedImageProduct.setProductId(UUID.randomUUID());
        populatedImageProduct.setPrice(50000);
        populatedImageProduct.setStock(10);
        populatedImageProduct.setImages(new ArrayList<>(List.of("img1.jpg", "img2.jpg")));
        Page<Product> productPage = new PageImpl<>(List.of(populatedImageProduct));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        ProductSearchCriteria criteria = ProductSearchCriteria.builder().jastiperId(jastiperId).build();

        var result = productService.getMyCatalog(criteria, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().getFirst().getImages().size());
        assertEquals("img1.jpg", result.getContent().getFirst().getImages().getFirst());
    }

    @Test
    void testUpdateProduct_WithNullImagesAndTags_Coverage() {
        UUID productId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();

        ProductUpdateRequest updateReq = new ProductUpdateRequest();
        updateReq.setName("New Name");
        updateReq.setPrice(1000L);

        Product product = new Product();
        product.setProductId(productId);
        product.setJastiperId(jastiperId);
        product.setName("Old Name");
        product.setDescription("Old Desc");
        product.setStatus(ProductStatus.ACTIVE);
        product.setPrice(500);
        product.setStock(10);
        product.setImages(null);
        product.setTags(null);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Optional<ProductResponse> response = productService.updateProduct(jastiperId, productId, updateReq);
        assertTrue(response.isPresent());
        verify(productRepository).findByIdForUpdate(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testGetMyCatalog_WithNullTags_Coverage() {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setJastiperId(UUID.randomUUID());
        product.setStatus(ProductStatus.ACTIVE);
        product.setPrice(1000);
        product.setStock(10);
        product.setTags(null);
        product.setImages(null);

        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        ProductSearchCriteria criteria = ProductSearchCriteria.builder().build();
        Page<ProductResponse> response = productService.getMyCatalog(criteria, PageRequest.of(0, 10));
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
}