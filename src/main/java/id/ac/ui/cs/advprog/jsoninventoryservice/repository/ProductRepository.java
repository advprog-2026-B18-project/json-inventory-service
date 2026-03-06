package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

}