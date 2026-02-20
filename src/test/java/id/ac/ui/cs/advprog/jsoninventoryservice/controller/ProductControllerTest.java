package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        Product p = new Product();
        p.setId("1");
        p.setName("Tas");
        p.setPrice(10000.0);
        p.setStock(5);

        when(productRepository.findAll()).thenReturn(Collections.singletonList(p));
        List<Product> result = productController.getAll();

        assertEquals(1, result.size());
        assertEquals("Tas", result.getFirst().getName());
        assertEquals(10000.0, result.getFirst().getPrice());
        assertEquals(5, result.getFirst().getStock());
        assertEquals("1", result.getFirst().getId());
    }

    @Test
    void testCreate() {
        Product p = new Product();
        p.setName("Sepatu");
        when(productRepository.save(any(Product.class))).thenReturn(p);

        Product result = productController.create(p);

        assertEquals("Sepatu", result.getName());
    }
}