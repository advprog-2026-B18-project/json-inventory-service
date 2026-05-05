package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.PostOrderRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReleaseRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReserveRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.StockOperationResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.StockReservation;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ReservationStatus;
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
class StockManagementServiceImplTest {
    @Mock private ProductRepository productRepository;
    @Mock private StockReservationRepository reservationRepository;
    @InjectMocks private StockManagementServiceImpl stockService;

    private Product product;
    private UUID productId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        product = new Product();
        product.setProductId(productId);
        product.setJastiperId(UUID.randomUUID());
        product.setName("Clothes");
        product.setDescription("Nice clothes");
        product.setPrice(10000);
        product.setStock(10);
        product.setOriginCountry("Japan");
        product.setPurchaseDate(LocalDate.now());
        product.setStatus(ProductStatus.ACTIVE);
        product.setAvgRating(0.0f);
        product.setTotalOrders(0);
        product.setTotalReviews(0);
    }

    @Test
    void reserveStock_IdempotentSuccess() {
        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(orderId);
        req.setQuantity(2);
        StockReservation existingRes = new StockReservation();
        existingRes.setReservationId(UUID.randomUUID());
        existingRes.setStatus(ReservationStatus.PENDING);
        existingRes.setQuantity(2);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.of(existingRes));
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        Optional<StockOperationResponse> res = stockService.reserveStock(productId, req);
        assertTrue(res.isPresent());
        verify(productRepository, never()).save(any());
    }

    @Test
    void reserveStock_Success_StockRemains() {
        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(orderId);
        req.setQuantity(2);
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(invocation -> {
            StockReservation saved = invocation.getArgument(0);
            saved.setReservationId(UUID.randomUUID());
            return saved;
        });

        Optional<StockOperationResponse> res = stockService.reserveStock(productId, req);
        assertTrue(res.isPresent());
        assertEquals(8, product.getStock());
    }

    @Test
    void reserveStock_Success_StockBecomesZero() {
        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(orderId);
        req.setQuantity(10);
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(invocation -> {
            StockReservation saved = invocation.getArgument(0);
            saved.setReservationId(UUID.randomUUID());
            return saved;
        });

        Optional<StockOperationResponse> res = stockService.reserveStock(productId, req);
        assertTrue(res.isPresent());
        assertEquals(0, product.getStock());
        assertEquals(ProductStatus.OUT_OF_STOCK, product.getStatus());
    }

    @Test
    void reserveStock_ExistingConfirmed_Branch() {
        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(orderId);
        req.setQuantity(2);
        StockReservation resMock = new StockReservation();
        resMock.setStatus(ReservationStatus.CONFIRMED);
        resMock.setReservationId(UUID.randomUUID());
        resMock.setQuantity(2);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.of(resMock));
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        Optional<StockOperationResponse> result = stockService.reserveStock(productId, req);
        assertTrue(result.isPresent());
    }

    @Test
    void reserveStock_ExistingIsReleased_Branch() {
        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(orderId);
        req.setQuantity(2);
        StockReservation oldRes = new StockReservation();
        oldRes.setStatus(ReservationStatus.RELEASED);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.of(oldRes));
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(invocation -> {
            StockReservation saved = invocation.getArgument(0);
            saved.setReservationId(UUID.randomUUID());
            return saved;
        });

        Optional<StockOperationResponse> res = stockService.reserveStock(productId, req);
        assertTrue(res.isPresent());
    }

    @Test
    void reserveStock_Fail_ProductNotActive() {
        product.setStatus(ProductStatus.HIDDEN);
        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(orderId);
        req.setQuantity(2);
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        Optional<StockOperationResponse> res = stockService.reserveStock(productId, req);
        assertFalse(res.isPresent());
    }

    @Test
    void reserveStock_Fail_InsufficientStock() {
        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(orderId);
        req.setQuantity(15);
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        Optional<StockOperationResponse> res = stockService.reserveStock(productId, req);
        assertFalse(res.isPresent());
    }

    @Test
    void releaseStock_Success_FromOutOfStockToActive() {
        product.setStock(0); product.setStatus(ProductStatus.OUT_OF_STOCK);
        StockReleaseRequest req = new StockReleaseRequest();
        req.setOrderId(orderId);
        req.setQuantity(5);
        StockReservation reservation = new StockReservation();
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setQuantity(5);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.of(reservation));
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));

        Optional<ProductResponse> res = stockService.releaseStock(productId, req);
        assertTrue(res.isPresent());
        assertEquals(5, product.getStock());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
    }

    @Test
    void releaseStock_Fail_NotFound() {
        StockReleaseRequest req = new StockReleaseRequest();
        req.setOrderId(orderId);
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.empty());
        Optional<ProductResponse> res = stockService.releaseStock(productId, req);
        assertFalse(res.isPresent());
    }

    @Test
    void releaseStock_Fail_AlreadyReleased() {
        StockReleaseRequest req = new StockReleaseRequest();
        req.setOrderId(orderId);
        StockReservation reservation = new StockReservation();
        reservation.setStatus(ReservationStatus.RELEASED);
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.of(reservation));
        Optional<ProductResponse> res = stockService.releaseStock(productId, req);
        assertFalse(res.isPresent());
    }

    @Test
    void processPostOrder_Confirm_Success() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");
        req.setRating(5.0);
        StockReservation res = new StockReservation();
        res.setStatus(ReservationStatus.PENDING);
        res.setProduct(product);

        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.of(res));

        Optional<ProductResponse> result = stockService.processPostOrder(productId, req);
        assertTrue(result.isPresent());
        assertEquals(ReservationStatus.CONFIRMED, res.getStatus());
        assertEquals(1, product.getTotalOrders());
        assertEquals(1, product.getTotalReviews());
        assertEquals(5.0f, product.getAvgRating());
        verify(reservationRepository).save(res);
        verify(productRepository).save(product);
    }

    @Test
    void processPostOrder_Cancel_Success() {
        product.setStock(0);
        product.setStatus(ProductStatus.OUT_OF_STOCK);
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CANCEL");
        StockReservation res = new StockReservation();
        res.setStatus(ReservationStatus.PENDING);
        res.setQuantity(2);
        res.setProduct(product);

        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.of(res));

        Optional<ProductResponse> result = stockService.processPostOrder(productId, req);
        assertTrue(result.isPresent());
        assertEquals(ReservationStatus.RELEASED, res.getStatus());
        assertEquals(2, product.getStock());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        verify(reservationRepository).save(res);
        verify(productRepository).save(product);
    }

    @Test
    void processPostOrder_Fail_NotFound() {
        PostOrderRequest req = new PostOrderRequest();
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.empty());
        Optional<ProductResponse> result = stockService.processPostOrder(productId, req);
        assertFalse(result.isPresent());
    }

    @Test
    void processPostOrder_Fail_InvalidAction() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("INVALID_ACTION");
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            stockService.processPostOrder(productId, req);
        });
    }

    @Test
    void processPostOrder_Confirm_OptResPresentButNotPending() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");
        StockReservation res = new StockReservation();
        res.setStatus(ReservationStatus.RELEASED);
        res.setProduct(product);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.of(res));
        stockService.processPostOrder(productId, req);
        assertEquals(ReservationStatus.RELEASED, res.getStatus());
    }

    @Test
    void processPostOrder_Confirm_RatingNull() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        stockService.processPostOrder(productId, req);
        assertEquals(0.0f, product.getAvgRating());
    }

    @Test
    void processPostOrder_Confirm_RatingOutOfBounds() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");
        req.setRating(6.0);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        stockService.processPostOrder(productId, req);
        assertEquals(0.0f, product.getAvgRating());
    }

    @Test
    void processPostOrder_Confirm_NullReviewsAndRating() {
        product.setTotalReviews(null);
        product.setAvgRating(null);
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");
        req.setRating(4.0);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        stockService.processPostOrder(productId, req);
        assertEquals(4.0f, product.getAvgRating());
        assertEquals(1, product.getTotalReviews());
    }

    @Test
    void processPostOrder_Cancel_OptResEmpty() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CANCEL");

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.empty());
        stockService.processPostOrder(productId, req);
        assertEquals(10, product.getStock());
    }

    @Test
    void processPostOrder_Cancel_OptResAlreadyReleased() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CANCEL");
        StockReservation res = new StockReservation();
        res.setStatus(ReservationStatus.RELEASED);
        res.setQuantity(2);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.of(res));
        stockService.processPostOrder(productId, req);
        assertEquals(10, product.getStock());
    }

    @Test
    void mapToResponse_WithNulls_Success() {
        Product dummyProduct = new Product();
        dummyProduct.setProductId(UUID.randomUUID());
        dummyProduct.setName("Dummy");
        dummyProduct.setPrice(10000);
        dummyProduct.setStock(5);
        dummyProduct.setImages(null);
        dummyProduct.setTags(null);

        ProductResponse res = ProductResponse.fromEntity(dummyProduct);
        assertNull(res.getImages());
        assertNull(res.getTags());
    }

    @Test
    void processPostOrder_Confirm_NullWrappersAndReservationEmpty() {
        product.setTotalOrders(null);
        product.setTotalReviews(null);
        product.setAvgRating(null);
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");
        req.setRating(3.0);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.empty());
        stockService.processPostOrder(productId, req);
        assertNull(product.getTotalOrders());
        assertEquals(1, product.getTotalReviews());
        assertEquals(3.0f, product.getAvgRating());
    }

    @Test
    void processPostOrder_Confirm_StatusNotPending() {
        StockReservation res = new StockReservation();
        res.setStatus(ReservationStatus.RELEASED);
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.of(res));
        stockService.processPostOrder(productId, req);
        assertEquals(ReservationStatus.RELEASED, res.getStatus());
    }

    @Test
    void releaseStock_ProductOutOfStock_NewStockStaysZero() {
        StockReservation res = new StockReservation();
        res.setQuantity(0);
        res.setStatus(ReservationStatus.PENDING);
        product.setStock(0);
        product.setStatus(ProductStatus.OUT_OF_STOCK);
        StockReleaseRequest req = new StockReleaseRequest();
        req.setOrderId(orderId);
        req.setQuantity(0);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.of(res));
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        stockService.releaseStock(productId, req);
        assertEquals(0, product.getStock());
        assertEquals(ProductStatus.OUT_OF_STOCK, product.getStatus());
    }

    @Test
    void processPostOrder_Confirm_RatingTooLow_Branch() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");
        req.setRating(0.5);
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        stockService.processPostOrder(productId, req);
        assertEquals(0.0f, product.getAvgRating());
    }

    @Test
    void processPostOrder_Confirm_RatingTooHigh_Branch() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");
        req.setRating(5.5);
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        stockService.processPostOrder(productId, req);
        assertEquals(0.0f, product.getAvgRating());
    }

    @Test
    void processPostOrder_Cancel_StatusAlreadyActive_Branch() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CANCEL");
        StockReservation res = new StockReservation();
        res.setStatus(ReservationStatus.PENDING);
        res.setQuantity(2);
        product.setStock(5); product.setStatus(ProductStatus.ACTIVE);
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.of(res));

        stockService.processPostOrder(productId, req);
        assertEquals(7, product.getStock());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    @Test
    void releaseStock_StatusAlreadyActive_Branch() {
        StockReleaseRequest req = new StockReleaseRequest();
        req.setOrderId(orderId);
        req.setQuantity(3);
        StockReservation res = new StockReservation();
        res.setStatus(ReservationStatus.PENDING);
        res.setQuantity(3);
        product.setStock(10); product.setStatus(ProductStatus.ACTIVE);
        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.of(res));
        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(product));

        stockService.releaseStock(productId, req);
        assertEquals(13, product.getStock());
    }

    @Test
    void processPostOrder_NullAction_Branch() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction(null);
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        assertThrows(IllegalArgumentException.class, () -> stockService.processPostOrder(productId, req));
    }

    @Test
    void processPostOrder_Cancel_StaysOutOfStock_Branch() {
        product.setStatus(ProductStatus.OUT_OF_STOCK);
        product.setStock(0);
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CANCEL");
        StockReservation res = new StockReservation();
        res.setStatus(ReservationStatus.PENDING);
        res.setQuantity(0);
        res.setProduct(product);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.of(res));
        stockService.processPostOrder(productId, req);
        assertEquals(0, product.getStock());
        assertEquals(ProductStatus.OUT_OF_STOCK, product.getStatus());
    }

    @Test
    void reserveStock_Fail_ProductNotActive_And_InsufficientStock() {
        product.setStatus(ProductStatus.HIDDEN);
        product.setStock(1);
        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(orderId);
        req.setQuantity(5);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(any(), any())).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        Optional<StockOperationResponse> res = stockService.reserveStock(productId, req);
        assertFalse(res.isPresent());
    }

    @Test
    void processPostOrder_Confirm_PendingRes_TotalOrdersNull_Branch() {
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(orderId);
        req.setAction("CONFIRM");
        StockReservation res = new StockReservation();
        product.setTotalOrders(null);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.of(res));
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        stockService.processPostOrder(productId, req);
        assertEquals(1, product.getTotalOrders());
        assertEquals(ReservationStatus.CONFIRMED, res.getStatus());
    }

    @Test
    void testReserveStock_Success() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Product product = new Product();
        product.setProductId(productId);
        product.setStock(10);
        product.setStatus(ProductStatus.ACTIVE);
        StockReserveRequest request = new StockReserveRequest();
        request.setOrderId(orderId);
        request.setQuantity(2);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(invocation -> {
            StockReservation res = invocation.getArgument(0);
            res.setReservationId(UUID.randomUUID());
            return res;
        });

        Optional<StockOperationResponse> response = stockService.reserveStock(productId, request);
        assertTrue(response.isPresent());
        assertEquals(2, response.get().getReservedQuantity());
        assertEquals(8, response.get().getRemainingStock());
        assertEquals("RESERVED", response.get().getStatus());
        assertNotNull(response.get().getReservationId());
        assertEquals(8, product.getStock());
    }

    @Test
    void testReleaseStock_WithNullImagesAndTags_Coverage() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        StockReleaseRequest req = new StockReleaseRequest();
        req.setOrderId(orderId);
        req.setQuantity(2);

        Product p = new Product();
        p.setProductId(productId);
        p.setJastiperId(UUID.randomUUID());
        p.setStatus(ProductStatus.ACTIVE);
        p.setPrice(10000);
        p.setStock(10);
        p.setImages(null);
        p.setTags(null);

        StockReservation res = new StockReservation();
        res.setProduct(p);
        res.setStatus(ReservationStatus.PENDING);
        res.setQuantity(2);

        when(reservationRepository.findByOrderIdAndProduct_ProductId(orderId, productId)).thenReturn(Optional.of(res));
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(p));
        Optional<ProductResponse> response = stockService.releaseStock(productId, req);
        assertTrue(response.isPresent());
    }
}