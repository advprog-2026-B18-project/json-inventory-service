package id.ac.ui.cs.advprog.jsoninventoryservice.specification;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductSearchCriteria;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> searchProducts(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            addExactMatches(predicates, cb, root, criteria);
            addRangeMatches(predicates, cb, root, criteria);
            addTextMatches(predicates, cb, root, criteria);

            predicates.add(cb.isNull(root.get("deletedAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addExactMatches(List<Predicate> predicates, CriteriaBuilder cb, Root<Product> root, ProductSearchCriteria criteria) {
        if (criteria.getStatus() != null) predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
        if (criteria.getJastiperId() != null) predicates.add(cb.equal(root.get("jastiperId"), criteria.getJastiperId()));
        if (criteria.getCategoryId() != null) predicates.add(cb.equal(root.get("categoryId"), criteria.getCategoryId()));
    }

    private static void addRangeMatches(List<Predicate> predicates, CriteriaBuilder cb, Root<Product> root, ProductSearchCriteria criteria) {
        if (criteria.getMinPrice() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice().intValue()));
        if (criteria.getMaxPrice() != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice().intValue()));
        if (criteria.getDateFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), criteria.getDateFrom()));
        if (criteria.getDateTo() != null) predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), criteria.getDateTo()));
    }

    private static void addTextMatches(List<Predicate> predicates, CriteriaBuilder cb, Root<Product> root, ProductSearchCriteria criteria) {
        if (criteria.getOriginCountry() != null && !criteria.getOriginCountry().trim().isEmpty()) {
            predicates.add(cb.equal(cb.lower(root.get("originCountry")), criteria.getOriginCountry().toLowerCase()));
        }

        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            String searchPattern = "%" + criteria.getKeyword().toLowerCase() + "%";
            Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
            Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
            predicates.add(cb.or(nameMatch, descMatch));
        }
    }
}