package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.exception.StockOperationException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ShoppingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingModeTests {

    private Product product;
    private LiveShoppingStrategy liveStrategy;
    private PreOrderStrategy preOrderStrategy;
    private FlashSaleStrategy flashSaleStrategy;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setStatus(ProductStatus.ACTIVE);
        product.setStock(10);

        liveStrategy = new LiveShoppingStrategy();
        preOrderStrategy = new PreOrderStrategy();
        flashSaleStrategy = new FlashSaleStrategy();
    }

    @Test
    void testLiveShopping_Eligible() {
        assertTrue(liveStrategy.isEligibleForReservation(product, 5));
    }

    @Test
    void testLiveShopping_NotEligible_StatusNotActive() {
        product.setStatus(ProductStatus.HIDDEN);
        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> liveStrategy.isEligibleForReservation(product, 5));
        assertEquals("This product is currently unavailable.", ex.getMessage());
    }

    @Test
    void testLiveShopping_NotEligible_InsufficientStock() {
        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> liveStrategy.isEligibleForReservation(product, 15));
        assertEquals("Insufficient stock.", ex.getMessage());
    }

    @Test
    void testPreOrder_Eligible_NullPurchaseDate() {
        product.setPurchaseDate(null);
        assertTrue(preOrderStrategy.isEligibleForReservation(product, 5));
    }

    @Test
    void testPreOrder_Eligible_FutureDate() {
        product.setPurchaseDate(LocalDate.now().plusDays(5));
        assertTrue(preOrderStrategy.isEligibleForReservation(product, 5));
    }

    @Test
    void testPreOrder_Eligible_TodayDate() {
        product.setPurchaseDate(LocalDate.now());
        assertTrue(preOrderStrategy.isEligibleForReservation(product, 5));
    }

    @Test
    void testPreOrder_NotEligible_PastDate() {
        product.setPurchaseDate(LocalDate.now().minusDays(1));
        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> preOrderStrategy.isEligibleForReservation(product, 5));
        assertEquals("Pre-order closed. Items are being purchased.", ex.getMessage());
    }

    @Test
    void testPreOrder_NotEligible_StatusNotActive() {
        product.setPurchaseDate(LocalDate.now().plusDays(5));
        product.setStatus(ProductStatus.OUT_OF_STOCK);
        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> preOrderStrategy.isEligibleForReservation(product, 5));
        assertEquals("This product is currently unavailable.", ex.getMessage());
    }

    @Test
    void testPreOrder_NotEligible_InsufficientStock() {
        product.setPurchaseDate(LocalDate.now().plusDays(5));
        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> preOrderStrategy.isEligibleForReservation(product, 15));
        assertEquals("Insufficient quota available.", ex.getMessage());
    }

    @Test
    void testFlashSale_Eligible() {
        product.setFlashSaleStart(LocalDateTime.now().minusHours(1));
        product.setFlashSaleEnd(LocalDateTime.now().plusHours(1));
        assertTrue(flashSaleStrategy.isEligibleForReservation(product, 5));
    }

    @Test
    void testFlashSale_NotEligible_NullStart() {
        product.setFlashSaleStart(null);
        product.setFlashSaleEnd(LocalDateTime.now().plusHours(1));
        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> flashSaleStrategy.isEligibleForReservation(product, 5));
        assertEquals("Invalid Flash Sale configuration.", ex.getMessage());
    }

    @Test
    void testFlashSale_NotEligible_NullEnd() {
        product.setFlashSaleStart(LocalDateTime.now().minusHours(1));
        product.setFlashSaleEnd(null);
        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> flashSaleStrategy.isEligibleForReservation(product, 5));
        assertEquals("Invalid Flash Sale configuration.", ex.getMessage());
    }

    @Test
    void testFlashSale_Throws_NotStartedYet() {
        product.setFlashSaleStart(LocalDateTime.now().plusHours(1));
        product.setFlashSaleEnd(LocalDateTime.now().plusHours(2));

        StockOperationException exception = assertThrows(StockOperationException.class,
                () -> flashSaleStrategy.isEligibleForReservation(product, 5));
        assertEquals("The flash sale hasn't started yet.", exception.getMessage());
    }

    @Test
    void testFlashSale_Throws_AlreadyEnded() {
        product.setFlashSaleStart(LocalDateTime.now().minusHours(2));
        product.setFlashSaleEnd(LocalDateTime.now().minusHours(1));

        StockOperationException exception = assertThrows(StockOperationException.class,
                () -> flashSaleStrategy.isEligibleForReservation(product, 5));
        assertEquals("The flash sale is over.", exception.getMessage());
    }

    @Test
    void testFlashSale_NotEligible_StatusNotActive() {
        product.setFlashSaleStart(LocalDateTime.now().minusHours(1));
        product.setFlashSaleEnd(LocalDateTime.now().plusHours(1));
        product.setStatus(ProductStatus.HIDDEN);

        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> flashSaleStrategy.isEligibleForReservation(product, 5));
        assertEquals("This product is currently unavailable.", ex.getMessage());
    }

    @Test
    void testFlashSale_NotEligible_InsufficientStock() {
        product.setFlashSaleStart(LocalDateTime.now().minusHours(1));
        product.setFlashSaleEnd(LocalDateTime.now().plusHours(1));

        StockOperationException ex = assertThrows(StockOperationException.class,
                () -> flashSaleStrategy.isEligibleForReservation(product, 15));
        assertEquals("Insufficient stock of Flash Sale items.", ex.getMessage());
    }

    @Test
    void testFactory_ReturnsCorrectStrategy() {
        ShoppingModeProvider factory = new ShoppingModeProvider(liveStrategy, preOrderStrategy, flashSaleStrategy);

        assertInstanceOf(LiveShoppingStrategy.class, factory.getStrategy(ShoppingMode.LIVE));
        assertInstanceOf(PreOrderStrategy.class, factory.getStrategy(ShoppingMode.PRE_ORDER));
        assertInstanceOf(FlashSaleStrategy.class, factory.getStrategy(ShoppingMode.FLASH_SALE));
        assertInstanceOf(LiveShoppingStrategy.class, factory.getStrategy(null));
    }
}