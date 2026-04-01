package id.ac.ui.cs.advprog.jsoninventoryservice.specification;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSpecificationTest {

    @Mock private Root<Product> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;
    @Mock private Predicate mockPredicate;
    @Mock private Path<Object> path;
    @Mock private Expression<String> stringExpression;

    @BeforeEach
    void setUp() {
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(cb.lower(any())).thenReturn(stringExpression);
        lenient().when(cb.like(any(Expression.class), anyString())).thenReturn(mockPredicate);
        lenient().when(cb.equal(any(), any())).thenReturn(mockPredicate);
        lenient().when(cb.greaterThanOrEqualTo(any(Expression.class), any(Comparable.class))).thenReturn(mockPredicate);
        lenient().when(cb.lessThanOrEqualTo(any(Expression.class), any(Comparable.class))).thenReturn(mockPredicate);
        lenient().when(cb.or(any(Predicate[].class))).thenReturn(mockPredicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mockPredicate);
    }

    @Test
    void testSearchByKeyword() {
        String keyword = "buku";
        Specification<Product> spec = ProductSpecification.searchProducts(keyword, null, null, null, null);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, atLeastOnce()).like(any(Expression.class), contains(keyword));
    }

    @Test
    void testSearchByPriceRange() {
        Long min = 1000L;
        Long max = 5000L;
        Specification<Product> spec = ProductSpecification.searchProducts(null, null, min, max, null);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, atLeastOnce()).greaterThanOrEqualTo(any(Expression.class), eq(min));
        verify(cb, atLeastOnce()).lessThanOrEqualTo(any(Expression.class), eq(max));
    }

    @Test
    void testSearchByStatus() {
        ProductStatus status = ProductStatus.ACTIVE;
        Specification<Product> spec = ProductSpecification.searchProducts(null, null, null, null, status);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, atLeastOnce()).equal(any(), eq(status));
    }

    @Test
    void testSearchWithFullParameters() {
        UUID sellerId = UUID.randomUUID();
        Specification<Product> spec = ProductSpecification.searchProducts("meja", sellerId, 100L, 500L, ProductStatus.ACTIVE);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, atLeastOnce()).and(any(Predicate[].class));
    }

    @Test
    void testSearchByKeywordEmptyOrBlank() {
        String emptyKeyword = "   ";
        Specification<Product> spec = ProductSpecification.searchProducts(emptyKeyword, null, null, null, null);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, never()).like(any(Expression.class), anyString());
    }

    @Test
    void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<ProductSpecification> constructor = ProductSpecification.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);

        java.lang.reflect.InvocationTargetException exception = assertThrows(
                java.lang.reflect.InvocationTargetException.class,
                constructor::newInstance
        );

        assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertEquals("Utility class", exception.getCause().getMessage());
    }
}