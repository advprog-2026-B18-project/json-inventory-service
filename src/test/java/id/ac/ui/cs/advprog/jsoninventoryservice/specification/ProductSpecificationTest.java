package id.ac.ui.cs.advprog.jsoninventoryservice.specification;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProductSpecificationTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testSearchProducts_AllFilters() {
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        when(cb.greaterThanOrEqualTo(any(Path.class), any(Integer.class))).thenReturn(mock(Predicate.class));
        when(cb.lessThanOrEqualTo(any(Path.class), any(Integer.class))).thenReturn(mock(Predicate.class));
        when(cb.greaterThanOrEqualTo(any(Path.class), any(LocalDate.class))).thenReturn(mock(Predicate.class));
        when(cb.lessThanOrEqualTo(any(Path.class), any(LocalDate.class))).thenReturn(mock(Predicate.class));
        when(cb.like(any(), anyString())).thenReturn(mock(Predicate.class));
        when(cb.or(any(), any())).thenReturn(mock(Predicate.class));
        when(cb.isNull(any())).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));
        when(cb.lower(any())).thenReturn(path);

        Specification<Product> spec = ProductSpecification.searchProducts("keyword", UUID.randomUUID(), 100L, 1000L, 1, ProductStatus.ACTIVE, "ID", LocalDate.now().minusDays(1), LocalDate.now());
        Predicate predicate = spec.toPredicate(root, query, cb);
        assertNotNull(predicate);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testSearchProducts_NoFilters() {
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.isNull(any())).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        Specification<Product> spec = ProductSpecification.searchProducts(null, null, null, null, null, null, null, null, null);
        Predicate predicate = spec.toPredicate(root, query, cb);
        assertNotNull(predicate);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testSearchProducts_EmptyStrings() {
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.isNull(any())).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        Specification<Product> spec = ProductSpecification.searchProducts("   ", null, null, null, null, null, "   ", null, null);
        Predicate predicate = spec.toPredicate(root, query, cb);
        assertNotNull(predicate);
    }

    @Test
    void testPrivateConstructor() {
        ProductSpecification spec = new ProductSpecification();
        assertNotNull(spec);
    }
}