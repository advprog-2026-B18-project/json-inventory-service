package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.jastiperId = :jastiperId " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findJastiperProductsWithFilters(
            @Param("jastiperId") UUID jastiperId,
            @Param("status") ProductStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findByProductIdAndJastiperId(UUID productId, UUID jastiperId);

}