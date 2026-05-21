package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.productId = :productId AND p.stock >= :quantity")
    int reserveStockAtomic(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.productId = :productId")
    int releaseStockAtomic(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.status = :status WHERE p.productId = :productId")
    void updateStatusAtomic(@Param("productId") UUID productId, @Param("status") ProductStatus status);
}