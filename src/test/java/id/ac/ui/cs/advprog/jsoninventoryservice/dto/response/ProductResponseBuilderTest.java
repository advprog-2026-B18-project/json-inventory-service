package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductResponseBuilderTest {

    @Test
    void testProductResponseBuilder() {
        UUID id = UUID.randomUUID();
        ProductResponse res = ProductResponse.builder()
                .productId(id)
                .name("Camera")
                .price(5000000)
                .stock(2)
                .status("ACTIVE")
                .build();

        assertNotNull(res);
        assertEquals(id, res.getProductId());
        assertEquals("Camera", res.getName());
        assertEquals(5000000, res.getPrice());
        assertEquals("ACTIVE", res.getStatus());
    }
}