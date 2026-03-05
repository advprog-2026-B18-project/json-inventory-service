package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
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

        entityManager.persistAndFlush(product1);
    }

    @Test
    void testFindById_Success() {
        Optional<Product> found = productRepository.findById(product1.getProductId());

        assertTrue(found.isPresent());
        assertEquals("Tas", found.get().getName());
    }

    @Test
    void testFindAll_Success() {
        List<Product> products = productRepository.findAll();

        assertFalse(products.isEmpty());
        assertEquals(1, products.size());
    }

    @Test
    void testSaveProduct_Success() {
        Product product2 = Product.builder()
                .jastiperId(jastiperId)
                .name("Jaket")
                .description("Warna Putih")
                .price(500000L)
                .stock(5)
                .originCountry("USA")
                .purchaseDate(LocalDate.now())
                .status(ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.save(product2);

        assertNotNull(savedProduct.getProductId());
        assertEquals("Jaket", savedProduct.getName());
    }

    @Test
    void testDeleteById_Success() {
        productRepository.deleteById(product1.getProductId());

        Optional<Product> found = productRepository.findById(product1.getProductId());
        assertFalse(found.isPresent());
    }
}