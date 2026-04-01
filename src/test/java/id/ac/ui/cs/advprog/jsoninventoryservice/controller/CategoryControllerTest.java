package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryServiceImpl categoryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void testGetAllCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());
    }
}