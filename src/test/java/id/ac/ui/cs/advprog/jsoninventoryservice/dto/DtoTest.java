package id.ac.ui.cs.advprog.jsoninventoryservice.dto;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DtoTest {

    @Test
    void testProductCreateRequest() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("Sepatu");
        req.setPrice(100000L);
        req.setStock(5);

        assertEquals("Sepatu", req.getName());
        assertEquals(100000L, req.getPrice());
        assertEquals(5, req.getStock());
    }

    @Test
    void testProductUpdateRequest() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("Tas Baru");
        req.setStatus("OUT_OF_STOCK");

        assertEquals("Tas Baru", req.getName());
        assertEquals("OUT_OF_STOCK", req.getStatus());
    }

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