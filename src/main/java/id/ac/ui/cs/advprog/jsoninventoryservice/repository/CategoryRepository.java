package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Standard CRUD JpaRepository (seperti findById dan findAll)
    // sudah cukup untuk kebutuhan pembuatan produk oleh Jastiper.

}