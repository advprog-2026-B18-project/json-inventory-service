package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import id.ac.ui.cs.advprog.jsoninventoryservice.security.JwtUtil;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID jastiperId;
    private UUID productId;
    private ProductResponse dummyResponse;

    @BeforeEach
    void setUp() {
        jastiperId = UUID.randomUUID();
        productId = UUID.randomUUID();
        dummyResponse = ProductResponse.builder()
                .id(productId)
                .jastiperId(jastiperId)
                .name("Controller Test Product")
                .price(20000L)
                .stock(10)
                .status("ACTIVE")
                .build();
    }

    @Test
    void testGetProductDetailPublic_Found() throws Exception {
        when(productService.getProductById(productId)).thenReturn(Optional.of(dummyResponse));

        mockMvc.perform(get("/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Controller Test Product"));
    }

    @Test
    void testCreateProduct() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("New");
        request.setPrice(100L);
        request.setStock(1);
        request.setOriginCountry("UK");
        request.setPurchaseDate(LocalDate.now());

        when(productService.createProduct(eq(jastiperId), any(ProductCreateRequest.class))).thenReturn(dummyResponse);

        mockMvc.perform(post("/products")
                        .requestAttr("jastiperId", jastiperId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productId.toString()));
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setPrice(500L);

        when(productService.updateProduct(eq(jastiperId), eq(productId), any(ProductUpdateRequest.class)))
                .thenReturn(Optional.of(dummyResponse));

        mockMvc.perform(patch("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteProduct_Success() throws Exception {
        when(productService.deleteProduct(jastiperId, productId)).thenReturn(true);

        mockMvc.perform(delete("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteProduct_NotFoundOrUnauthorized() throws Exception {
        when(productService.deleteProduct(jastiperId, productId)).thenReturn(false);

        mockMvc.perform(delete("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testReserveStock_Success() throws Exception {
        when(productService.reserveStock(productId, 2)).thenReturn(Optional.of(dummyResponse));

        mockMvc.perform(post("/products/internal/" + productId + "/stock/reserve")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetProductDetailPublic_NotFound() throws Exception {
        when(productService.getProductById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Product not found")));
    }

    @Test
    void testUpdateProduct_Unauthorized() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("New Name");

        when(productService.updateProduct(eq(jastiperId), eq(productId), any()))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    @Test
    void testGetMyCatalog() throws Exception {
        when(productService.getMyCatalog(eq(jastiperId), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(dummyResponse)));

        mockMvc.perform(get("/products/my")
                        .requestAttr("jastiperId", jastiperId)
                        .param("search", "sepatu")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyProductDetail() throws Exception {
        when(productService.getProductById(productId))
                .thenReturn(java.util.Optional.of(dummyResponse));

        mockMvc.perform(get("/products/my/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchProducts() throws Exception {
        when(productService.searchProductsPublic(any(), any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(dummyResponse)));

        mockMvc.perform(get("/products")
                        .param("q", "baju")
                        .param("minPrice", "10000")
                        .param("maxPrice", "50000")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchByJastiper() throws Exception {
        when(productService.searchProductsPublic(any(), eq(jastiperId), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(dummyResponse)));

        mockMvc.perform(get("/products/jastipers/" + jastiperId)
                        .param("q", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}