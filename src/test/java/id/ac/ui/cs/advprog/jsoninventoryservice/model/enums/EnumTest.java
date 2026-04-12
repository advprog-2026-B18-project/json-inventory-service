package id.ac.ui.cs.advprog.jsoninventoryservice.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumTest {

    @Test
    void testProductStatus() {
        ProductStatus[] values = ProductStatus.values();
        assertTrue(values.length > 0);
        assertEquals(ProductStatus.ACTIVE, ProductStatus.valueOf("ACTIVE"));
        assertEquals(ProductStatus.OUT_OF_STOCK, ProductStatus.valueOf("OUT_OF_STOCK"));
        assertEquals(ProductStatus.HIDDEN, ProductStatus.valueOf("HIDDEN"));
        assertEquals(ProductStatus.REMOVED_BY_ADMIN, ProductStatus.valueOf("REMOVED_BY_ADMIN"));
    }

    @Test
    void testModerationAction() {
        ModerationAction[] values = ModerationAction.values();
        assertTrue(values.length > 0);
        assertEquals(ModerationAction.REMOVE, ModerationAction.valueOf("REMOVE"));
        assertEquals(ModerationAction.RESTORE, ModerationAction.valueOf("RESTORE"));
        assertEquals(ModerationAction.HIDE, ModerationAction.valueOf("HIDE"));
        assertEquals(ModerationAction.ACTIVATE, ModerationAction.valueOf("ACTIVATE"));
    }

    @Test
    void testReservationStatus() {
        ReservationStatus[] values = ReservationStatus.values();
        assertTrue(values.length > 0);
        assertEquals(ReservationStatus.PENDING, ReservationStatus.valueOf("PENDING"));
        assertEquals(ReservationStatus.CONFIRMED, ReservationStatus.valueOf("CONFIRMED"));
        assertEquals(ReservationStatus.RELEASED, ReservationStatus.valueOf("RELEASED"));
    }
}