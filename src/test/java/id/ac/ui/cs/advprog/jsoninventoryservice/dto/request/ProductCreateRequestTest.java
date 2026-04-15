package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductCreateRequestTest {
    @Test
    void testProductCreateRequest() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("Shoes");
        req.setPrice(100000L);
        req.setStock(5);

        assertEquals("Shoes", req.getName());
        assertEquals(100000L, req.getPrice());
        assertEquals(5, req.getStock());
    }
}