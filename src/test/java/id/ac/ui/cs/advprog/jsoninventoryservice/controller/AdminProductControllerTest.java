package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.AdminProductUpdateRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.AdminProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminProductService adminProductService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private UUID adminId;
    private UUID productId;
    private ProductResponse dummyResponse;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        adminId = UUID.randomUUID();
        productId = UUID.randomUUID();
        dummyResponse = new ProductResponse();
        dummyResponse.setProductId(productId);
        dummyResponse.setName("Admin Prod");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllProductsAdmin_Success() throws Exception {
        when(adminProductService.getAllProductsAdmin(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(dummyResponse)));
        mockMvc.perform(get("/admin/products")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAdminProductDetail_Success() throws Exception {
        when(adminProductService.getAdminProductDetail(productId)).thenReturn(Optional.of(dummyResponse));
        mockMvc.perform(get("/admin/products/" + productId)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAdminProductDetail_NotFound() throws Exception {
        when(adminProductService.getAdminProductDetail(productId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/products/" + productId)).andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void moderateProduct_Success() throws Exception {
        when(adminProductService.moderateProduct(any(UUID.class), eq(productId), any(AdminProductUpdateRequest.class))).thenReturn(Optional.of(dummyResponse));
        mockMvc.perform(patch("/admin/products/" + productId + "/moderate")
                        .requestAttr("adminId", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"HIDE\", \"reason\":\"Violates rules\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void moderateProduct_BadRequest() throws Exception {
        when(adminProductService.moderateProduct(any(UUID.class), eq(productId), any(AdminProductUpdateRequest.class))).thenThrow(new IllegalArgumentException("Invalid action"));
        mockMvc.perform(patch("/admin/products/" + productId + "/moderate")
                        .requestAttr("adminId", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"UNKNOWN\", \"reason\":\"Violates rules\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}