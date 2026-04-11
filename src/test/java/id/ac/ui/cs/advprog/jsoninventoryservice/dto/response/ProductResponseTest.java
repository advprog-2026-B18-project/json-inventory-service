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
        product.setId(UUID.randomUUID());
        product.setJastiperId(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Desc");
        product.setPrice(100L);
        product.setStock(10);
        product.setOriginCountry("JP");
        product.setPurchaseDate(LocalDate.now());
        product.setImages(List.of("img1.png"));
        product.setTags(List.of("tag1"));
        product.setStatus(ProductStatus.ACTIVE);
        product.setAvgRating(4.5);
        product.setTotalOrders(5);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setServiceFee(5000L);
        product.setWeightGram(250);

        ProductResponse res = ProductResponse.fromEntity(product);

        assertEquals(product.getId(), res.getId());
        assertEquals(250, res.getWeightGram());
        assertFalse(res.getImages().isEmpty());
        assertFalse(res.getTags().isEmpty());
    }

    @Test
    void testFromEntity_NullCollections() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setStatus(ProductStatus.OUT_OF_STOCK);
        product.setImages(null);
        product.setTags(null);

        ProductResponse res = ProductResponse.fromEntity(product);

        assertNotNull(res.getImages());
        assertTrue(res.getImages().isEmpty());
        assertNotNull(res.getTags());
        assertTrue(res.getTags().isEmpty());
    }
}