package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.ModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModerationLogRepository extends JpaRepository<ModerationLog, UUID> {

    // Interface ini dibiarkan ada HANYA agar Spring Data JPA
    // otomatis membuatkan tabel 'moderation_logs' di database.

}