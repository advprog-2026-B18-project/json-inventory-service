package id.ac.ui.cs.advprog.jsoninventoryservice.specification;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductSpecification {
    public static Specification<Product> searchProducts(String keyword, UUID jastiperId, Long minPrice, Long maxPrice, Integer categoryId, ProductStatus status, String originCountry, LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (jastiperId != null) predicates.add(cb.equal(root.get("jastiperId"), jastiperId));
            if (categoryId != null) predicates.add(cb.equal(root.get("categoryId"), categoryId));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice.intValue()));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice.intValue()));
            if (originCountry != null && !originCountry.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("originCountry")), originCountry.toLowerCase()));
            }
            if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), dateFrom));
            if (dateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), dateTo));
            if (keyword != null && !keyword.trim().isEmpty()) {
                Predicate ftsMatch = cb.isTrue(cb.function(
                        "sql", Boolean.class,
                        cb.literal("to_tsvector('indonesian', {alias}.name || ' ' || {alias}.description) @@ plainto_tsquery('indonesian', ?)"),
                        cb.literal(keyword)
                ));
                predicates.add(ftsMatch);
            }

            predicates.add(cb.isNull(root.get("deletedAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}