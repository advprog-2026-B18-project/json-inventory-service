package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductResponseTest {

    @Test
    void testFromEntity_AllFields() {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setJastiperId(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Desc");
        product.setPrice(100);
        product.setStock(10);
        product.setOriginCountry("JP");
        product.setPurchaseDate(LocalDate.now());
        product.setImages(List.of("img1.png"));
        product.setTags(List.of("tag1"));
        product.setStatus(ProductStatus.ACTIVE);
        product.setAvgRating(4.5f);
        product.setTotalOrders(5);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setServiceFee(5000);
        product.setWeightGram(250);

        ProductResponse res = ProductResponse.fromEntity(product);

        assertEquals(product.getProductId(), res.getProductId());
        assertEquals(250, res.getWeightGram());
        assertFalse(res.getImages().isEmpty());
        assertFalse(res.getTags().isEmpty());
    }

    @Test
    void testFromEntity_NullCollections() {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setStatus(ProductStatus.OUT_OF_STOCK);
        product.setImages(null);
        product.setTags(null);
        product.setPrice(50000);
        product.setStock(5);

        ProductResponse res = ProductResponse.fromEntity(product);

        assertNull(res.getImages());
        assertNull(res.getTags());
    }

    @Test
    void testFromEntity_WithValidNullables() {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setPrice(150000);
        product.setStock(10);

        product.setStatus(ProductStatus.HIDDEN);
        product.setServiceFee(5000);
        product.setTotalOrders(20);
        product.setTotalReviews(15);

        product.setAvgRating(4.8f);
        product.setCategoryId(99);

        ProductResponse res = ProductResponse.fromEntity(product);

        assertEquals("HIDDEN", res.getStatus());
        assertEquals(5000L, res.getServiceFee());
        assertEquals(20, res.getStats().getTotalOrders());
        assertEquals(15, res.getStats().getTotalReviews());

        assertEquals(4.8, res.getStats().getAvgRating(), 0.001);

        assertNotNull(res.getCategory());
        assertEquals(99, res.getCategory().getId());
    }

    @Test
    void testFromEntity_ExplicitNullFields_Branch() {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setPrice(150000);
        product.setStock(10);

        product.setStatus(null);
        product.setServiceFee(null);
        product.setTotalReviews(null);

        ProductResponse res = ProductResponse.fromEntity(product);

        assertNull(res.getStatus());
        assertNull(res.getServiceFee());
        assertEquals(0, res.getStats().getTotalReviews());
    }
}