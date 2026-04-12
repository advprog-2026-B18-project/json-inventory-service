package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductUpdateRequestTest {

    @Test
    void testProductUpdateRequest() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("Tas Baru");
        req.setStatus("OUT_OF_STOCK");

        assertEquals("Tas Baru", req.getName());
        assertEquals("OUT_OF_STOCK", req.getStatus());
    }
}