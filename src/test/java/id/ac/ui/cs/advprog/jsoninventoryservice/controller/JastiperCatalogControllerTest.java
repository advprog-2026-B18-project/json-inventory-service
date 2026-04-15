package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.AuthIntegrationService;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JastiperCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class JastiperCatalogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService searchService;

    @MockitoBean
    private AuthIntegrationService authIntegrationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void getJastiperCatalog_Success() throws Exception {
        UUID jastiperId = UUID.randomUUID();
        when(authIntegrationService.getJastiperIdByUsername("jastiper123")).thenReturn(jastiperId);

        ProductResponse res = ProductResponse.builder().name("Tumbler").build();
        Page<ProductResponse> page = new PageImpl<>(List.of(res));

        when(searchService.searchProductsPublic(any(), eq(jastiperId), any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/jastipers/jastiper123/products")
                        .param("q", "Tumbler")
                        .param("min_price", "100")
                        .param("max_price", "1000")
                        .param("category_id", "1")
                        .param("origin_country", "Japan")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched jastiper catalog successfully."))
                .andExpect(jsonPath("$.data.data").isArray())
                .andExpect(jsonPath("$.data.pagination.total").value(1));
    }
}