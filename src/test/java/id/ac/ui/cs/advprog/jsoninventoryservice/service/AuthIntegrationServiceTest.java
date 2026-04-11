package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthIntegrationServiceTest {

    @Mock private RestTemplate restTemplate;
    @InjectMocks private AuthIntegrationServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "authServiceUrl", "http://localhost:8082");
    }

    @Test
    void getJastiperId_Success_WithId() {
        UUID expectedId = UUID.randomUUID();
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("id", expectedId.toString());
        mockResponse.put("data", data);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        assertEquals(expectedId, authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Success_WithUserId() {
        UUID expectedId = UUID.randomUUID();
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", expectedId.toString());
        mockResponse.put("data", data);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        assertEquals(expectedId, authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Success_WithUser_id() {
        UUID expectedId = UUID.randomUUID();
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", expectedId.toString());
        mockResponse.put("data", data);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        assertEquals(expectedId, authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Success_WithAccountId() {
        UUID expectedId = UUID.randomUUID();
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("accountId", expectedId.toString());
        mockResponse.put("data", data);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        assertEquals(expectedId, authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Fail_ResponseNull() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);
        assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Fail_SuccessFalse() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", false);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Fail_MissingData() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Fail_MissingIdKey() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("randomKey", "123");
        mockResponse.put("data", data);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Fail_NotFound() {
        HttpClientErrorException.NotFound mockNotFoundException = org.mockito.Mockito.mock(HttpClientErrorException.NotFound.class);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(mockNotFoundException);

        assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("budi"));
    }

    @Test
    void getJastiperId_Fail_ConnectionError() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));
        assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("budi"));
    }
}