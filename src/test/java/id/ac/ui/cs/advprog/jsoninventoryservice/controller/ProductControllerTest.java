package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductCreateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.ActiveOrderException;
import id.ac.ui.cs.advprog.jsoninventoryservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private UUID jastiperId;
    private UUID productId;
    private ProductResponse dummyResponse;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("jastiper", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_JASTIPER")))
        );

        jastiperId = UUID.randomUUID();
        productId = UUID.randomUUID();

        dummyResponse = new ProductResponse();
        dummyResponse.setProductId(productId);
        dummyResponse.setJastiper(ProductResponse.JastiperInfo.builder().userId(jastiperId).build());        dummyResponse.setName("Test Product");
        dummyResponse.setPrice(100);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetProductDetailPublic_Found() throws Exception {
        when(productService.getProductById(productId)).thenReturn(Optional.of(dummyResponse));

        mockMvc.perform(get("/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    void testGetProductDetailPublic_NotFound() throws Exception {
        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/" + productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testCreateProduct() throws Exception {
        when(productService.createProduct(any(), any(ProductCreateRequest.class))).thenReturn(dummyResponse);

        String validPayload = "{"
                + "\"name\":\"Test Product\","
                + "\"description\":\"Valid Description\","
                + "\"price\":100,"
                + "\"stock\":10,"
                + "\"origin_country\":\"Japan\","
                + "\"purchase_date\":\"2026-01-01\""
                + "}";

        mockMvc.perform(post("/products")
                        .requestAttr("jastiperId", jastiperId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSearchProducts() throws Exception {
        PageImpl<ProductResponse> page = new PageImpl<>(Collections.singletonList(dummyResponse));

        when(productService.searchProductsPublic(any(), any())).thenReturn(page);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        when(productService.updateProduct(any(), eq(productId), any(ProductUpdateRequest.class)))
                .thenReturn(Optional.of(dummyResponse));

        mockMvc.perform(patch("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteProduct_Success() throws Exception {
        when(productService.deleteProduct(any(), eq(productId))).thenReturn(true);

        mockMvc.perform(delete("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "JASTIPER")
    void testDeleteProduct_NotFoundOrUnauthorized() throws Exception {
        when(productService.deleteProduct(any(), eq(productId)))
                .thenThrow(new IllegalArgumentException("Product not found"));

        mockMvc.perform(delete("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateProduct_Unauthorized() throws Exception {
        when(productService.updateProduct(any(), eq(productId), any())).thenReturn(Optional.empty());

        mockMvc.perform(patch("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetMyCatalog() throws Exception {
        PageImpl<ProductResponse> page = new PageImpl<>(Collections.singletonList(dummyResponse));
        when(productService.getMyCatalog(any(), any())).thenReturn(page);

        mockMvc.perform(get("/products/my")
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetMyProductDetail_Success() throws Exception {
        when(productService.getProductById(productId)).thenReturn(Optional.of(dummyResponse));

        mockMvc.perform(get("/products/my/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetMyProductDetail_UnauthorizedOrNotFound() throws Exception {
        ProductResponse wrongOwnerResponse = new ProductResponse();
        when(productService.getProductById(productId)).thenReturn(Optional.of(wrongOwnerResponse));

        mockMvc.perform(get("/products/my/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "JASTIPER")
    void deleteProduct_Conflict() throws Exception {
        when(productService.deleteProduct(any(), eq(productId)))
                .thenThrow(new ActiveOrderException(5));

        mockMvc.perform(delete("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "JASTIPER")
    void updateProduct_IllegalArgumentException_Branch() throws Exception {
        when(productService.updateProduct(any(), eq(productId), any()))
                .thenThrow(new IllegalArgumentException("Invalid status"));

        mockMvc.perform(patch("/products/" + productId)
                        .requestAttr("jastiperId", jastiperId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "JASTIPER")
    void testGetMyProductDetail_NullJastiperInfo() throws Exception {
        ProductResponse noJastiperResponse = new ProductResponse();
        noJastiperResponse.setProductId(productId);
        noJastiperResponse.setJastiper(null);

        when(productService.getProductById(productId)).thenReturn(Optional.of(noJastiperResponse));

        mockMvc.perform(get("/products/my/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "JASTIPER")
    void testGetMyProductDetail_NullJastiperObject() throws Exception {
        ProductResponse responseWithNullJastiper = new ProductResponse();
        responseWithNullJastiper.setProductId(productId);
        responseWithNullJastiper.setJastiper(null);

        when(productService.getProductById(productId)).thenReturn(Optional.of(responseWithNullJastiper));

        mockMvc.perform(get("/products/my/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "JASTIPER")
    void testGetMyProductDetail_WrongJastiperId_Branch() throws Exception {
        ProductResponse wrongOwnerResponse = new ProductResponse();
        wrongOwnerResponse.setProductId(productId);
        wrongOwnerResponse.setJastiper(ProductResponse.JastiperInfo.builder().userId(UUID.randomUUID()).build());

        when(productService.getProductById(productId)).thenReturn(Optional.of(wrongOwnerResponse));

        mockMvc.perform(get("/products/my/" + productId)
                        .requestAttr("jastiperId", jastiperId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchProducts_SortByRating_Ascending() throws Exception {
        when(productService.searchProductsPublic(any(),any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/products")
                        .param("sort_by", "rating")
                        .param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    @Test
    void testGetMyCatalog_WithEmptyStatus() throws Exception {
        when(productService.getMyCatalog(any(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/products/my")
                        .requestAttr("jastiperId", jastiperId)
                        .param("status", "   "))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyCatalog_WithInvalidStatus_HitsCatchBlock() throws Exception {
        when(productService.getMyCatalog(any(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/products/my")
                        .requestAttr("jastiperId", jastiperId)
                        .param("status", "STATUS_YANG_SALAH"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyCatalog_WithNullStatus() throws Exception {
        when(productService.getMyCatalog(any(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/products/my").requestAttr("jastiperId", jastiperId))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyCatalog_WithValidStatus() throws Exception {
        when(productService.getMyCatalog(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/products/my")
                        .requestAttr("jastiperId", jastiperId)
                        .param("status", "active"))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchProducts_DefaultSort_CreatedAt_Desc() throws Exception {
        when(productService.searchProductsPublic(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.get("/products"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    @Test
    void testSearchProducts_SortByPurchaseDate_Asc() throws Exception {
        when(productService.searchProductsPublic(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .param("sort_by", "purchase_date")
                        .param("sortBy", "purchase_date")
                        .param("order", "asc"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testSearchProducts_SortByRating_Desc() throws Exception {
        when(productService.searchProductsPublic(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .param("sort_by", "rating")
                        .param("sortBy", "rating")
                        .param("order", "desc"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testSearchProducts_SortByOther_Asc() throws Exception {
        when(productService.searchProductsPublic(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .param("sort_by", "price")
                        .param("sortBy", "price")
                        .param("order", "asc"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}