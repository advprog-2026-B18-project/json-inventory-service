package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.AdminProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.event.ProductModeratedEvent;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.ModerationLog;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ModerationAction;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ModerationLogRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"unchecked", "rawtypes"})
class AdminProductServiceImplTest {
    @Mock private ProductRepository productRepository;
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
        assertEquals(ProductStatus.HIDDEN, product.getStatus());
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> adminService.moderateProduct(adminId, productId, req));
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
}