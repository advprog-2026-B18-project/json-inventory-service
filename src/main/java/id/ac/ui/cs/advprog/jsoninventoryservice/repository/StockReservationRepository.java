package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    Optional<StockReservation> findByOrderIdAndProduct_Id(UUID orderId, UUID productId);
    @Query("SELECT r FROM StockReservation r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    List<StockReservation> findExpiredReservations(@Param("now") LocalDateTime now);
    long countByProduct_IdAndStatus(UUID id, ReservationStatus status);
}