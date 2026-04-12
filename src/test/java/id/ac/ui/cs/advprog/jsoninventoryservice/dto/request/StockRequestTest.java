package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StockRequestTest {
    @Test
    void testStockReserveRequest() {
        StockReserveRequest r1 = new StockReserveRequest();
        UUID id = UUID.randomUUID();
        r1.setOrderId(id); r1.setQuantity(5);
        assertEquals(id, r1.getOrderId());
        assertEquals(5, r1.getQuantity());

        StockReserveRequest r2 = new StockReserveRequest();
        r2.setOrderId(id); r2.setQuantity(5);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());
    }

    @Test
    void testStockReleaseRequest() {
        StockReleaseRequest r1 = new StockReleaseRequest();
        UUID id = UUID.randomUUID();
        r1.setOrderId(id); r1.setQuantity(5);
        assertEquals(id, r1.getOrderId());
        assertEquals(5, r1.getQuantity());

        StockReleaseRequest r2 = new StockReleaseRequest();
        r2.setOrderId(id); r2.setQuantity(5);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());
    }
}