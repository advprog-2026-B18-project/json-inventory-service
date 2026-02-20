package id.ac.ui.cs.advprog.jsoninventoryservice.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductTest {

    @Test
    void testProductGettersAndSetters() {
        Product product = new Product();

        assertNull(product.getId());

        product.setId("uuid-1234");
        product.setName("Tas");
        product.setPrice(150000.0);
        product.setStock(10);

        assertEquals("uuid-1234", product.getId());
        assertEquals("Tas", product.getName());
        assertEquals(150000.0, product.getPrice());
        assertEquals(10, product.getStock());
    }
}