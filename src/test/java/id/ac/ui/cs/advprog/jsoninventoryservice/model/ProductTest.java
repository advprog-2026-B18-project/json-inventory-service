package id.ac.ui.cs.advprog.jsoninventoryservice.model;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    private Product product;

    @BeforeEach
    void setUp() {
        UUID jastiperId = UUID.randomUUID();
        product = Product.builder()
                .jastiperId(jastiperId)
                .name("Shoes")
                .description("sport shoes")
                .price(500000)
                .stock(5)
                .originCountry("Indonesia")
                .purchaseDate(LocalDate.now())
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void testProductCreation() {
        assertNotNull(product);
        assertEquals("Shoes", product.getName());
        assertEquals(500000, product.getPrice());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertTrue(product.getTotalOrders() == null || product.getTotalOrders() == 0);
        assertTrue(product.getTotalReviews() == null || product.getTotalReviews() == 0);
    }
}