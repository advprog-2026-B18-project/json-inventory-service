package id.ac.ui.cs.advprog.jsoninventoryservice.model;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ModerationAction;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModelCreationTest {

    @Test
    void testCategoryCreation() {
        Category category = new Category();
        category.setCategoryId(1);
        category.setName("Elektronik");
        category.setSlug("elektronik");
        category.setDescription("Barang elektronik");
        category.setProductCount(0);
        category.setCreatedAt(LocalDateTime.now());

        assertNotNull(category);
        assertEquals("Elektronik", category.getName());
    }

    @Test
    void testModerationLogCreation() {
        Product dummyProduct = new Product();
        ModerationLog log = new ModerationLog();
        log.setLogId(UUID.randomUUID());
        log.setProduct(dummyProduct);
        log.setAdminId(UUID.randomUUID());
        log.setAction(ModerationAction.REMOVE);
        log.setReason("Melanggar aturan");
        log.setCreatedAt(LocalDateTime.now());

        assertNotNull(log);
        assertEquals(ModerationAction.REMOVE, log.getAction());
    }

    @Test
    void testStockReservationCreation() {
        Product dummyProduct = new Product();
        StockReservation reservation = new StockReservation();
        reservation.setReservationId(UUID.randomUUID());
        reservation.setProduct(dummyProduct);
        reservation.setOrderId(UUID.randomUUID());
        reservation.setQuantity(2);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        assertNotNull(reservation);
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }

    @Test
    void testCategoryPrePersist() {
        Category category = new Category();
        assertNull(category.getCreatedAt());

        category.onCreate();

        assertNotNull(category.getCreatedAt());
    }

    @Test
    void testModerationLogPrePersist() {
        ModerationLog log = new ModerationLog();
        assertNull(log.getCreatedAt());

        log.onCreate();

        assertNotNull(log.getCreatedAt());
    }

    @Test
    void testStockReservationPrePersist() {
        StockReservation reservation = new StockReservation();
        assertNull(reservation.getCreatedAt());

        reservation.onCreate();

        assertNotNull(reservation.getCreatedAt());
    }
}