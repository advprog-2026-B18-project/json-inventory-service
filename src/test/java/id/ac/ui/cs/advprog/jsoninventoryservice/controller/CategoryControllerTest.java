package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.CategoryRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_Success() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("New Category");
        req.setDescription("Valid category");

        CategoryResponse res = CategoryResponse.builder().name("New Category").build();
        when(categoryService.createCategory(any())).thenReturn(res);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("New Category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_Conflict() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Exist");
        when(categoryService.createCategory(any())).thenThrow(new IllegalArgumentException("Exists"));

        mockMvc.perform(post("/admin/categories").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_Success() throws Exception {
        mockMvc.perform(delete("/admin/categories/{id}", 1)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("Category not found")).when(categoryService).deleteCategory(1);
        mockMvc.perform(delete("/admin/categories/{id}", 1)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_Conflict() throws Exception {
        doThrow(new IllegalStateException("In Use")).when(categoryService).deleteCategory(1);
        mockMvc.perform(delete("/admin/categories/{id}", 1))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCategory_Success() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Fashion");

        CategoryResponse response = CategoryResponse.builder().categoryId(1).name("Updated Fashion").slug("updated-fashion").build();
        when(categoryService.updateCategory(eq(1), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Fashion"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCategory_NotFound() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Fashion");

        when(categoryService.updateCategory(eq(99), any(CategoryRequest.class)))
                .thenThrow(new IllegalArgumentException("Category not found"));

        mockMvc.perform(patch("/admin/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCategory_Conflict() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Existing Category");

        when(categoryService.updateCategory(eq(1), any(CategoryRequest.class)))
                .thenThrow(new id.ac.ui.cs.advprog.jsoninventoryservice.exception.DuplicateResourceException("Category name already exists."));

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}