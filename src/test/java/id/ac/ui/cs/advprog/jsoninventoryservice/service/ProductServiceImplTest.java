package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product dummyProduct;
    private UUID jastiperId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        jastiperId = UUID.randomUUID();
        productId = UUID.randomUUID();
        dummyProduct = Product.builder()
                .productId(productId)
                .jastiperId(jastiperId)
                .name("Test Product")
                .description("Desc")
                .price(100L)
                .stock(10)
                .originCountry("ID")
                .purchaseDate(LocalDate.now())
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void testGetAllProductsPublic_OnlyActive() {
        Product hiddenProduct = Product.builder().status(ProductStatus.HIDDEN).build();
        when(productRepository.findAll()).thenReturn(List.of(dummyProduct, hiddenProduct));

        List<ProductResponse> responses = productService.getAllProductsPublic();

        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getName());
    }

    @Test
    void testGetMyProducts() {
        when(productRepository.findAll()).thenReturn(List.of(dummyProduct));

        List<ProductResponse> responses = productService.getMyProducts(jastiperId);

        assertEquals(1, responses.size());
        assertEquals(productId, responses.get(0).getProductId());
    }

    @Test
    void testCreateProduct() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("New Prod");
        req.setPrice(50L);
        req.setStock(5);
        req.setOriginCountry("JP");
        req.setPurchaseDate(LocalDate.now());

        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        ProductResponse response = productService.createProduct(jastiperId, req);

        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("Updated Name");
        req.setStock(20);

        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        Optional<ProductResponse> response = productService.updateProduct(jastiperId, productId, req);

        assertTrue(response.isPresent());
        assertEquals("Updated Name", dummyProduct.getName());
        assertEquals(20, dummyProduct.getStock());
    }

    @Test
    void testUpdateProduct_FailWrongOwner() {
        UUID wrongJastiperId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.updateProduct(wrongJastiperId, productId, new ProductUpdateRequest());

        assertFalse(response.isPresent());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        boolean result = productService.deleteProduct(jastiperId, productId);

        assertTrue(result);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void testDeleteProduct_FailWrongOwner() {
        UUID wrongJastiperId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        boolean result = productService.deleteProduct(wrongJastiperId, productId);

        assertFalse(result);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void testReserveStock_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));
        when(productRepository.save(any(Product.class))).thenReturn(dummyProduct);

        Optional<ProductResponse> response = productService.reserveStock(productId, 2);

        assertTrue(response.isPresent());
        assertEquals(8, dummyProduct.getStock());
    }

    @Test
    void testReserveStock_FailInsufficientStock() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(dummyProduct));

        Optional<ProductResponse> response = productService.reserveStock(productId, 20);

        assertFalse(response.isPresent());
        assertEquals(10, dummyProduct.getStock());
    }
}