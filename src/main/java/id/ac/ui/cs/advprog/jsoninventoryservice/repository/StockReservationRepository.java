package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.StockReservation;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    @Query("SELECT sr FROM StockReservation sr WHERE sr.orderId = :orderId AND sr.product.productId = :productId")
    Optional<StockReservation> findByOrderIdAndProduct_ProductId(@Param("orderId") UUID orderId, @Param("productId") UUID productId);

    @Query("SELECT COUNT(sr) FROM StockReservation sr WHERE sr.product.productId = :productId AND sr.status IN :statuses")
    long countByProductProductIdAndStatusIn(@Param("productId") UUID productId, @Param("statuses") List<ReservationStatus> statuses);

    @Query("SELECT r FROM StockReservation r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    List<StockReservation> findExpiredReservations(@Param("now") LocalDateTime now);
}