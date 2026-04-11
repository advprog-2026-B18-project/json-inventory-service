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
                .id(id)
                .name("Kamera")
                .price(5000000L)
                .stock(2)
                .status("ACTIVE")
                .build();

        assertNotNull(res);
        assertEquals(id, res.getId());
        assertEquals("Kamera", res.getName());
        assertEquals(5000000L, res.getPrice());
        assertEquals("ACTIVE", res.getStatus());
    }
}