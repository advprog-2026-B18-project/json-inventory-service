package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private UUID jastiperId;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        jastiperId = UUID.randomUUID();

        product1 = Product.builder()
                .jastiperId(jastiperId)
                .name("Tas")
                .description("Warna Hitam")
                .price(300000L)
                .stock(10)
                .originCountry("Japan")
                .purchaseDate(LocalDate.now())
                .status(ProductStatus.ACTIVE)
                .build();

        product2 = Product.builder()
                .jastiperId(jastiperId)
                .name("Jaket")
                .description("Warna Putih")
                .price(500000L)
                .stock(0)
                .originCountry("USA")
                .purchaseDate(LocalDate.now())
                .status(ProductStatus.OUT_OF_STOCK)
                .build();

        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
    }

    @Test
    void testFindByProductIdAndJastiperId_Success() {
        Optional<Product> found = productRepository.findByProductIdAndJastiperId(product1.getProductId(), jastiperId);

        assertTrue(found.isPresent());
        assertEquals("Tas", found.get().getName());
    }

    @Test
    void testFindJastiperProductsWithFilters_NoFilters() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findJastiperProductsWithFilters(jastiperId, null, null, pageRequest);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testFindJastiperProductsWithFilters_StatusFilter() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findJastiperProductsWithFilters(jastiperId, ProductStatus.OUT_OF_STOCK, null, pageRequest);

        assertEquals(1, result.getTotalElements());
        assertEquals("Jaket", result.getContent().get(0).getName());
    }

    @Test
    void testFindJastiperProductsWithFilters_SearchFilter() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findJastiperProductsWithFilters(jastiperId, null, "tas", pageRequest);

        assertEquals(1, result.getTotalElements());
        assertEquals("Tas", result.getContent().get(0).getName());
    }
}