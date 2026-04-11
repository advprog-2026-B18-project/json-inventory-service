package id.ac.ui.cs.advprog.jsoninventoryservice.specification;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductSpecificationTest {

    @Mock private Root<Product> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;
    @Mock private Path<Object> path;
    @Mock private Path<Object> categoryPath;
    @Mock private Predicate predicate;

    @BeforeEach
    void setUp() {
        when(root.get(anyString())).thenReturn(path);
        when(path.get(anyString())).thenReturn(categoryPath);
        when(cb.isNull(any())).thenReturn(predicate);
        when(cb.like(any(), anyString())).thenReturn(predicate);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(any(Expression.class), any(Comparable.class))).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(Expression.class), any(Comparable.class))).thenReturn(predicate);
        when(cb.lower(any())).thenReturn((Expression) path);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void testSearchProducts_AllFilters() {
        Specification<Product> spec = ProductSpecification.searchProducts("keyword", UUID.randomUUID(), 100L, 1000L, 1, ProductStatus.ACTIVE);
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
    }

    @Test
    void testSearchProducts_NoFilters() {
        Specification<Product> spec = ProductSpecification.searchProducts(null, null, null, null, null, null);
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
    }

    @Test
    void testSearchByCategoryId() {
        Specification<Product> spec = ProductSpecification.searchProducts(null, null, null, null, 1, null);
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
    }

    @Test
    void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<ProductSpecification> constructor = ProductSpecification.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Expected InvocationTargetException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertInstanceOf(IllegalStateException.class, e.getCause());
        }
    }

    @Test
    void testSearchProducts_BlankKeyword_Branch() {
        org.springframework.data.jpa.domain.Specification<id.ac.ui.cs.advprog.jsoninventoryservice.model.Product> spec =
                ProductSpecification.searchProducts("   ", null, null, null, null, null);

        jakarta.persistence.criteria.Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
    }
}