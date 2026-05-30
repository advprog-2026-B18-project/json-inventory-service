package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.AdminProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.event.ProductModeratedEvent;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Category;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.ModerationLog;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ModerationAction;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ModerationLogRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"unchecked", "rawtypes"})
class AdminProductServiceImplTest {
    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AuthIntegrationService authIntegrationService;
    @Mock private ModerationLogRepository moderationLogRepository;
    @Mock private StockReservationRepository stockReservationRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private Root<Product> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;
    @Mock private Path<Object> path;
    @Mock private Predicate predicate;
    @InjectMocks private AdminProductServiceImpl adminService;

    private Product product;
    private UUID productId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        product = new Product();
        product.setProductId(productId);
        product.setName("Test Product");
        product.setJastiperId(UUID.randomUUID());
        product.setStatus(ProductStatus.ACTIVE);
        product.setPrice(150000);
        product.setStock(10);
        product.setImages(List.of("image1.jpg"));
        product.setTags(List.of("tag1"));

        when(root.get(anyString())).thenReturn(path);
        when(path.get(anyString())).thenReturn(path);
        when(cb.like(any(), anyString())).thenReturn(predicate);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.isNotNull(any())).thenReturn(predicate);
        when(cb.isNull(any())).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        when(cb.lower(any())).thenReturn((Expression) path);
    }

    @Test
    void getAllProductsAdmin_Success_WithVariousFilters() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        adminService.getAllProductsAdmin("keyword", UUID.randomUUID(), "ACTIVE", 1, PageRequest.of(0, 10));
        adminService.getAllProductsAdmin("   ", null, "REMOVED_BY_ADMIN", null, PageRequest.of(0, 10));

        ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(productRepository, times(2)).findAll(specCaptor.capture(), any(PageRequest.class));

        List<Specification<Product>> specs = specCaptor.getAllValues();
        specs.get(0).toPredicate(root, query, cb);
        specs.get(1).toPredicate(root, query, cb);
    }

    @Test
    void getAdminProductDetail_Success() {
        ModerationLog log = new ModerationLog();
        log.setLogId(UUID.randomUUID());
        log.setAction(ModerationAction.HIDE);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(moderationLogRepository.findByProduct_ProductIdOrderByCreatedAtDesc(productId)).thenReturn(List.of(log));

        Optional<ProductResponse> res = adminService.getAdminProductDetail(productId);
        assertTrue(res.isPresent());
    }

    @Test
    void moderateProduct_Remove_Success() {
        AdminProductUpdateRequest req = new AdminProductUpdateRequest();
        req.setAction("REMOVE");
        req.setReason("Breaking the rules");

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(stockReservationRepository.countByProductProductIdAndStatusIn(eq(productId), anyList())).thenReturn(0L);
        when(productRepository.save(any())).thenReturn(product);
        doNothing().when(eventPublisher).publishEvent(any(ProductModeratedEvent.class));

        Optional<ProductResponse> res = adminService.moderateProduct(adminId, productId, req);
        assertTrue(res.isPresent());
        assertNotNull(product.getDeletedAt());
        assertEquals(ProductStatus.REMOVED_BY_ADMIN, product.getStatus());
        verify(moderationLogRepository).save(any(ModerationLog.class));
        verify(eventPublisher, times(1)).publishEvent(any(ProductModeratedEvent.class));
    }

    @Test
    void moderateProduct_Hide_Success() {
        AdminProductUpdateRequest req = new AdminProductUpdateRequest();
        req.setAction("HIDE");
        req.setReason("User reports");

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);
        doNothing().when(eventPublisher).publishEvent(any(ProductModeratedEvent.class));

        Optional<ProductResponse> res = adminService.moderateProduct(adminId, productId, req);
        assertTrue(res.isPresent());
        assertEquals(ProductStatus.HIDDEN, product.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(ProductModeratedEvent.class));
    }

    @Test
    void getAllProductsAdmin_TestSpecificationBranches() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        adminService.getAllProductsAdmin("   ", null, null, null, PageRequest.of(0, 10));
        adminService.getAllProductsAdmin(null, null, "   ", null, PageRequest.of(0, 10));
        adminService.getAllProductsAdmin(null, null, "REMOVE", null, PageRequest.of(0, 10));

        ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(productRepository, times(3)).findAll(specCaptor.capture(), any(PageRequest.class));

        List<Specification<Product>> specs = specCaptor.getAllValues();
        specs.get(0).toPredicate(root, query, cb);
        specs.get(1).toPredicate(root, query, cb);
        specs.get(2).toPredicate(root, query, cb);
    }

    @Test
    void moderateProduct_Activate_Success() {
        AdminProductUpdateRequest req = new AdminProductUpdateRequest();
        req.setAction("ACTIVATE");
        req.setReason("Passed review");
        product.setStatus(ProductStatus.HIDDEN);
        product.setDeletedAt(LocalDateTime.now());

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);
        doNothing().when(eventPublisher).publishEvent(any(ProductModeratedEvent.class));

        Optional<ProductResponse> res = adminService.moderateProduct(adminId, productId, req);
        assertTrue(res.isPresent());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertNull(product.getDeletedAt());
        verify(moderationLogRepository).save(any(ModerationLog.class));
        verify(eventPublisher, times(1)).publishEvent(any(ProductModeratedEvent.class));
    }

    @Test
    void moderateProduct_InvalidAction_ThrowsException() {
        AdminProductUpdateRequest req = new AdminProductUpdateRequest();
        req.setAction("INVALID");
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> adminService.moderateProduct(adminId, productId, req));
        assertTrue(exception.getMessage().contains("REMOVE, RESTORE, HIDE, ACTIVATE"));
    }

    @Test
    void moderateProduct_Restore_Success() {
        AdminProductUpdateRequest req = new AdminProductUpdateRequest();
        req.setAction("RESTORE");
        req.setReason("Passed review");

        product.setStatus(ProductStatus.HIDDEN);
        product.setDeletedAt(LocalDateTime.now());

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);
        doNothing().when(eventPublisher).publishEvent(any(ProductModeratedEvent.class));

        Optional<ProductResponse> res = adminService.moderateProduct(adminId, productId, req);
        assertTrue(res.isPresent());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertNull(product.getDeletedAt());
        verify(moderationLogRepository).save(any(ModerationLog.class));
        verify(eventPublisher, times(1)).publishEvent(any(ProductModeratedEvent.class));
    }

    @Test
    void testGetAdminProductDetail_WithNullImagesAndTags_Coverage() {
        UUID localProductId = UUID.randomUUID();
        Product p = new Product();
        p.setProductId(localProductId);
        p.setJastiperId(UUID.randomUUID());
        p.setStatus(ProductStatus.ACTIVE);
        p.setPrice(150000);
        p.setStock(10);
        p.setImages(null);
        p.setTags(null);

        when(productRepository.findById(localProductId)).thenReturn(Optional.of(p));

        Optional<ProductResponse> response = adminService.getAdminProductDetail(localProductId);
        assertTrue(response.isPresent());
        assertNull(response.get().getImages());
    }

    @Test
    void testEnrichProductResponse_WithCategoryAndCompleteProfile() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        UUID mockJastiperId = UUID.randomUUID();

        Product adminProduct = Product.builder()
                .productId(UUID.randomUUID())
                .jastiperId(mockJastiperId)
                .categoryId(1)
                .name("Barang Admin")
                .price(150000)
                .stock(10)
                .serviceFee(2000)
                .weightGram(500)
                .build();

        Category dummyCategory = new Category();
        dummyCategory.setCategoryId(1);
        dummyCategory.setName("Elektronik");

        // Dari server: pakai key "rating" + nested "stats" dengan total_orders
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("total_orders", 15);

        Map<String, Object> mockProfile = new HashMap<>();
        mockProfile.put("username", "admin_jastip");
        mockProfile.put("full_name", "Admin Jastip");
        mockProfile.put("profile_picture_url", "http://image.png");
        mockProfile.put("rating", 4.8);
        mockProfile.put("stats", mockStats);

        Page<Product> page = new PageImpl<>(List.of(adminProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(dummyCategory));
        when(authIntegrationService.getJastiperProfile(mockJastiperId)).thenReturn(mockProfile);

        Page<ProductResponse> result = adminService.getAllProductsAdmin(keyword, null, null, null, pageable);

        assertFalse(result.getContent().isEmpty());
        ProductResponse response = result.getContent().get(0);
        assertEquals("Elektronik", response.getCategory().getName());
        assertEquals("admin_jastip", response.getJastiper().getUsername());
        assertEquals(4.8, response.getJastiper().getAvgRating());
    }

    @Test
    void testEnrichProductResponse_AuthServiceThrowsException_HitsCatchBlock() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        UUID mockJastiperId = UUID.randomUUID();

        Product adminProduct = Product.builder()
                .productId(UUID.randomUUID())
                .jastiperId(mockJastiperId)
                .name("Barang Admin")
                .price(150000)
                .stock(10)
                .serviceFee(2000)
                .weightGram(500)
                .build();

        Page<Product> page = new PageImpl<>(List.of(adminProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(authIntegrationService.getJastiperProfile(mockJastiperId))
                .thenThrow(new RuntimeException("Auth Service Down"));

        Page<ProductResponse> result = adminService.getAllProductsAdmin(keyword, null, null, null, pageable);

        assertFalse(result.getContent().isEmpty());
        assertNull(result.getContent().get(0).getJastiper().getUsername());
    }

    @Test
    void testEnrichProductResponse_ProfileMissingAvgRating() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        UUID mockJastiperId = UUID.randomUUID();

        Product adminProduct = Product.builder()
                .productId(UUID.randomUUID())
                .jastiperId(mockJastiperId)
                .name("Barang Test")
                .price(5000)
                .stock(5)
                .serviceFee(1000)
                .weightGram(100)
                .build();

        Map<String, Object> mockProfile = new HashMap<>();
        mockProfile.put("username", "jastiper_no_rating");
        mockProfile.put("full_name", "Jastiper No Rating");

        Page<Product> page = new PageImpl<>(List.of(adminProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(authIntegrationService.getJastiperProfile(mockJastiperId)).thenReturn(mockProfile);

        Page<ProductResponse> result = adminService.getAllProductsAdmin(keyword, null, null, null, pageable);

        assertFalse(result.getContent().isEmpty());
        assertNull(result.getContent().get(0).getJastiper().getAvgRating());
    }

    @Test
    void testEnrichProductResponse_ProfileIsEmptyOrNull() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        UUID mockJastiperId = UUID.randomUUID();

        Product adminProduct = Product.builder()
                .productId(UUID.randomUUID())
                .jastiperId(mockJastiperId)
                .name("Barang Test")
                .price(5000)
                .stock(5)
                .serviceFee(1000)
                .weightGram(100)
                .build();

        Page<Product> page = new PageImpl<>(List.of(adminProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        when(authIntegrationService.getJastiperProfile(mockJastiperId)).thenReturn(new HashMap<>());
        Page<ProductResponse> resultEmpty = adminService.getAllProductsAdmin(keyword, null, null, null, pageable);
        assertFalse(resultEmpty.getContent().isEmpty());

        when(authIntegrationService.getJastiperProfile(mockJastiperId)).thenReturn(null);
        Page<ProductResponse> resultNull = adminService.getAllProductsAdmin(keyword, null, null, null, pageable);
        assertFalse(resultNull.getContent().isEmpty());
    }

    @Test
    void testEnrichProductResponse_CategoryNotFoundAndNullChecking() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        UUID mockJastiperId = UUID.randomUUID();

        Product adminProduct = Product.builder()
                .productId(UUID.randomUUID())
                .jastiperId(mockJastiperId)
                .categoryId(999)
                .name("Barang Test")
                .price(5000)
                .stock(5)
                .serviceFee(1000)
                .weightGram(100)
                .build();

        Page<Product> page = new PageImpl<>(List.of(adminProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());
        when(authIntegrationService.getJastiperProfile(mockJastiperId)).thenReturn(null);

        Page<ProductResponse> result = adminService.getAllProductsAdmin(keyword, null, null, null, pageable);
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    void testEnrichProductResponse_CategoryAndJastiperAlreadyNonNull_CoverFalseBranches() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        UUID mockJastiperId = UUID.randomUUID();

        Product adminProduct = Product.builder()
                .productId(UUID.randomUUID())
                .jastiperId(mockJastiperId)
                .categoryId(1)
                .name("Barang Sisa Coverage")
                .price(5000)
                .stock(5)
                .serviceFee(1000)
                .weightGram(100)
                .build();

        Category dummyCategory = new Category();
        dummyCategory.setCategoryId(1);
        dummyCategory.setName("Elektronik Baru");

        Map<String, Object> mockProfile = new HashMap<>();
        mockProfile.put("username", "jastiper_override");

        Page<Product> page = new PageImpl<>(List.of(adminProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(dummyCategory));
        when(authIntegrationService.getJastiperProfile(mockJastiperId)).thenReturn(mockProfile);

        ProductResponse preInitializedResponse = new ProductResponse();
        preInitializedResponse.setCategory(ProductResponse.CategoryInfo.builder().id(1).build());
        preInitializedResponse.setJastiper(ProductResponse.JastiperInfo.builder().userId(mockJastiperId).build());

        try (MockedStatic<ProductResponse> mockedResponse = mockStatic(ProductResponse.class)) {
            mockedResponse.when(() -> ProductResponse.fromEntity(any(Product.class)))
                    .thenReturn(preInitializedResponse);

            Page<ProductResponse> result = adminService.getAllProductsAdmin(keyword, null, null, null, pageable);

            assertFalse(result.getContent().isEmpty());
            ProductResponse response = result.getContent().get(0);
            assertEquals("Elektronik Baru", response.getCategory().getName());
            assertEquals("jastiper_override", response.getJastiper().getUsername());
        }
    }

    @Test
    void testEnrichProductResponse_NullCategoryAndJastiper_CoversTrueBranches() {
        UUID mockJastiperId = UUID.randomUUID();
        Product adminProduct = new Product();
        adminProduct.setProductId(UUID.randomUUID());
        adminProduct.setCategoryId(3);
        adminProduct.setJastiperId(mockJastiperId);

        Category dummyCategory = new Category();
        dummyCategory.setCategoryId(3);
        dummyCategory.setName("Category 3");

        when(productRepository.findById(adminProduct.getProductId())).thenReturn(Optional.of(adminProduct));
        when(categoryRepository.findById(3)).thenReturn(Optional.of(dummyCategory));
        when(authIntegrationService.getJastiperProfile(mockJastiperId)).thenReturn(null);

        ProductResponse emptyResponse = new ProductResponse();

        try (MockedStatic<ProductResponse> mocked = mockStatic(ProductResponse.class)) {
            mocked.when(() -> ProductResponse.fromEntity(any(Product.class))).thenReturn(emptyResponse);

            Optional<ProductResponse> result = adminService.getAdminProductDetail(adminProduct.getProductId());

            assertTrue(result.isPresent());
            assertNotNull(result.get().getCategory());
            assertEquals(3, result.get().getCategory().getId());
            assertEquals("Category 3", result.get().getCategory().getName());
            assertNotNull(result.get().getJastiper());
            assertEquals(mockJastiperId, result.get().getJastiper().getUserId());
        }
    }
}