package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    // Interface ini dibiarkan ada HANYA agar Spring Data JPA
    // otomatis membuatkan tabel 'stock_reservations' di database.

}