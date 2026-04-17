package id.ac.ui.cs.advprog.jsoninventoryservice.specification;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductSearchCriteria;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class ProductSpecificationTest {
    @Test
    void testConstructorIsPrivate() throws Exception {
        Constructor<ProductSpecification> constructor = ProductSpecification.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ProductSpecification instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void testSpecification_WithAllRangeAndTextCriteria() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .minPrice(100L)
                .maxPrice(500L)
                .dateFrom(LocalDate.now().minusDays(1))
                .dateTo(LocalDate.now())
                .originCountry("US")
                .keyword("laptop")
                .status(ProductStatus.ACTIVE)
                .categoryId(1)
                .jastiperId(UUID.randomUUID())
                .build();

        Specification<Product> spec = ProductSpecification.searchProducts(criteria);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        Root<Product> root = mock(Root.class);
        Path<Object> path = mock(Path.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        when(cb.greaterThanOrEqualTo(any(Expression.class), any(Integer.class))).thenReturn(mock(Predicate.class));
        when(cb.lessThanOrEqualTo(any(Expression.class), any(Integer.class))).thenReturn(mock(Predicate.class));
        when(cb.greaterThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(mock(Predicate.class));
        when(cb.lessThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(mock(Predicate.class));
        when(cb.lower(any())).thenReturn(mock(Expression.class));
        when(cb.literal(any())).thenReturn(mock(Expression.class));
        when(cb.function(anyString(), eq(Boolean.class), any(), any())).thenReturn(mock(Expression.class));
        when(cb.isTrue(any())).thenReturn(mock(Predicate.class));
        when(cb.isNull(any())).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        Predicate result = spec.toPredicate(root, cq, cb);
        assertNotNull(result);
    }

    @Test
    void testSpecification_WithNullRangeAndEmptyTextCriteria() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .minPrice(null)
                .maxPrice(null)
                .dateFrom(null)
                .dateTo(null)
                .originCountry("   ")
                .keyword("   ")
                .status(null)
                .categoryId(null)
                .jastiperId(null)
                .build();

        Specification<Product> spec = ProductSpecification.searchProducts(criteria);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        Root<Product> root = mock(Root.class);
        Path<Object> path = mock(Path.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.isNull(any())).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        Predicate result = spec.toPredicate(root, cq, cb);
        assertNotNull(result);
    }
}