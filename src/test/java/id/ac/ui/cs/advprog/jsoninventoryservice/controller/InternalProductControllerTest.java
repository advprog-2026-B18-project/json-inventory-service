package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.PostOrderRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReleaseRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReserveRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.StockOperationResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.StockManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockManagementService stockService;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reserveStock_Success() throws Exception {
        UUID productId = UUID.randomUUID();
        StockReserveRequest req = new StockReserveRequest();
        req.setQuantity(2);

        Product product = new Product();
        product.setProductId(productId);
        product.setStatus(ProductStatus.ACTIVE);
        product.setStock(10);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));

        when(stockService.reserveStock(eq(productId), any())).thenReturn(Optional.of(
                StockOperationResponse.builder()
                        .productId(productId)
                        .reservedQuantity(2)
                        .remainingStock(8)
                        .status("RESERVED")
                        .build()
        ));

        mockMvc.perform(post("/internal/products/{id}/stock/reserve", productId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void reserveStock_Fail() throws Exception {
        UUID productId = UUID.randomUUID();
        StockReserveRequest req = new StockReserveRequest();
        req.setQuantity(20);

        Product product = new Product();
        product.setProductId(productId);
        product.setStatus(ProductStatus.ACTIVE);
        product.setStock(10);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));

        mockMvc.perform(post("/internal/products/{id}/stock/reserve", productId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void releaseStock_Success() throws Exception {
        UUID id = UUID.randomUUID();
        StockReleaseRequest req = new StockReleaseRequest(); req.setQuantity(2); req.setOrderId(id);
        ProductResponse res = ProductResponse.builder().productId(id).build();
        when(stockService.releaseStock(eq(id), any())).thenReturn(Optional.of(res));

        mockMvc.perform(post("/internal/products/{id}/stock/release", id)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void releaseStock_Fail() throws Exception {
        UUID id = UUID.randomUUID();
        StockReleaseRequest req = new StockReleaseRequest();
        when(stockService.releaseStock(eq(id), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/internal/products/{id}/stock/release", id)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void processPostOrder_Success() throws Exception {
        UUID id = UUID.randomUUID();
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(UUID.randomUUID());
        req.setAction("CONFIRM");
        req.setRating(5.0);

        ProductResponse res = ProductResponse.builder().productId(id).build();
        when(stockService.processPostOrder(eq(id), any(PostOrderRequest.class))).thenReturn(Optional.of(res));

        mockMvc.perform(post("/internal/products/{id}/post-order", id)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post-order processed successfully."));
    }

    @Test
    void processPostOrder_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(UUID.randomUUID());
        req.setAction("CONFIRM");

        when(stockService.processPostOrder(eq(id), any(PostOrderRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/internal/products/{id}/post-order", id)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void processPostOrder_BadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        PostOrderRequest req = new PostOrderRequest();
        req.setOrderId(UUID.randomUUID());
        req.setAction("INVALID_ACTION");

        when(stockService.processPostOrder(eq(id), any(PostOrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Action is invalid"));

        mockMvc.perform(post("/internal/products/{id}/post-order", id)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void reserveStock_NotFound_ThrowsException() throws Exception {
        UUID productId = UUID.randomUUID();
        StockReserveRequest req = new StockReserveRequest();
        req.setQuantity(2);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/internal/products/{id}/stock/reserve", productId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void reserveStock_NotActive_ThrowsException() throws Exception {
        UUID productId = UUID.randomUUID();
        StockReserveRequest req = new StockReserveRequest();
        req.setQuantity(2);

        Product product = new Product();
        product.setProductId(productId);
        product.setStock(10);
        product.setStatus(ProductStatus.OUT_OF_STOCK);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));

        mockMvc.perform(post("/internal/products/{id}/stock/reserve", productId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testReserveStock_ReservationFailed_Returns500() throws Exception {
        Product p = new Product();
        p.setStatus(ProductStatus.ACTIVE);
        p.setStock(10);

        when(productRepository.findByIdForUpdate(any())).thenReturn(Optional.of(p));
        when(stockService.reserveStock(any(), any())).thenReturn(Optional.empty());

        StockReserveRequest req = new StockReserveRequest();
        req.setOrderId(UUID.randomUUID());
        req.setQuantity(2);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(post("/internal/products/" + UUID.randomUUID() + "/stock/reserve")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
}