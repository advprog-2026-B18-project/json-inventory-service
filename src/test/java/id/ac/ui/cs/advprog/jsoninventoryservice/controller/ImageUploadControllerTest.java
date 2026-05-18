package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.exception.ImageUploadException;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImageUploadControllerTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ImageUploadController imageUploadController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageUploadController).build();
    }

    @Test
    void testUploadImageSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/mock-uuid.jpg";

        when(storageService.uploadFile(any())).thenReturn(expectedUrl);

        mockMvc.perform(multipart("/internal/products/images/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Image uploaded successfully"))
                .andExpect(jsonPath("$.data.image_url").value(expectedUrl));

        verify(storageService, times(1)).uploadFile(any());
    }

    @Test
    void testUploadImageThrowsIOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        when(storageService.uploadFile(any())).thenThrow(new IOException("S3 connection error"));

        ServletException exception = assertThrows(ServletException.class, () -> {
            mockMvc.perform(multipart("/internal/products/images/upload")
                    .file(file));
        });

        assertInstanceOf(ImageUploadException.class, exception.getRootCause());
        assertTrue(exception.getRootCause().getMessage().contains("Failed to upload image to S3"));

        verify(storageService, times(1)).uploadFile(any());
    }
}