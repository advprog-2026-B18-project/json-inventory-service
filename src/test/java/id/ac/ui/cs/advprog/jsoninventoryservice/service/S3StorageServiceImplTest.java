package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3StorageServiceImpl s3StorageService;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3StorageService, "bucketName", bucketName);
    }

    @Test
    void testUploadFileWithExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "test data".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String result = s3StorageService.uploadFile(file);

        assertNotNull(result);
        assertTrue(result.startsWith("https://" + bucketName + ".s3.amazonaws.com/"));
        assertTrue(result.endsWith(".png"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFileWithNullOriginalFilename() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("test data".getBytes()));
        when(mockFile.getSize()).thenReturn(9L);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String result = s3StorageService.uploadFile(mockFile);

        assertNotNull(result);
        assertTrue(result.startsWith("https://" + bucketName + ".s3.amazonaws.com/"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFileWithoutDotInFilename() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "gambar_polos", "image/jpeg", "test data".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String result = s3StorageService.uploadFile(file);

        assertNotNull(result);
        assertTrue(result.startsWith("https://" + bucketName + ".s3.amazonaws.com/"));
        assertFalse(result.substring(result.lastIndexOf("/") + 1).contains("."));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}