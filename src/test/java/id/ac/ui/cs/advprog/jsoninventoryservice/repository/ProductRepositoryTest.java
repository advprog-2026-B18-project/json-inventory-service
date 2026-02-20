package id.ac.ui.cs.advprog.jsoninventoryservice.repository;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testSaveAndFindAll() {
        Product product = new Product();
        product.setName("Kacamata");
        product.setPrice(50000.0);
        product.setStock(2);

        Product savedProduct = productRepository.save(product);
        assertNotNull(savedProduct.getId());

        List<Product> products = productRepository.findAll();
        assertEquals(1, products.size());
        assertEquals("Kacamata", products.getFirst().getName());
    }
}