package id.ac.ui.cs.advprog.jsoninventoryservice.model;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;
    private UUID jastiperId;

    @BeforeEach
    void setUp() {
        jastiperId = UUID.randomUUID();
        product = Product.builder()
                .jastiperId(jastiperId)
                .name("Sepatu Compass")
                .description("Sepatu lokal pride")
                .price(500000L)
                .stock(5)
                .originCountry("Indonesia")
                .purchaseDate(LocalDate.now())
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void testProductCreation() {
        assertNotNull(product);
        assertEquals("Sepatu Compass", product.getName());
        assertEquals(500000L, product.getPrice());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(0, product.getTotalOrders());
        assertEquals(0, product.getTotalReviews());
    }

    @Test
    void testPrePersist() {
        assertNull(product.getCreatedAt());
        assertNull(product.getUpdatedAt());

        product.onCreate();

        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    void testPreUpdate() {
        product.onCreate();
        var oldUpdatedAt = product.getUpdatedAt();

        product.onUpdate();

        assertNotNull(product.getUpdatedAt());
        assertTrue(product.getUpdatedAt().isAfter(oldUpdatedAt) || product.getUpdatedAt().isEqual(oldUpdatedAt));
    }
}