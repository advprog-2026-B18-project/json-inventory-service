package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class AuthIntegrationServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthIntegrationServiceImpl authService;

    private final String username = "testuser";
    private final UUID jastiperId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "authServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(authService, "restTemplate", restTemplate);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "userId", "user_id", "accountId"})
    void testGetJastiperId_Success_VariousIdKeys(String idKey) {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put(idKey, jastiperId.toString());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", mockData);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        UUID result = authService.getJastiperIdByUsername(username);
        assertEquals(jastiperId, result);
    }

    @Test
    void testGetJastiperIdByUsername_ActiveAccount_Success() {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", jastiperId.toString());
        mockData.put("status", "ACTIVE");
        mockData.put("is_active", true);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", mockData);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        UUID result = authService.getJastiperIdByUsername(username);
        assertEquals(jastiperId, result);
    }

    @Test
    void testGetJastiperIdByUsername_MissingStatusAndIsActiveKeys_Success() {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", jastiperId.toString());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", mockData);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        UUID result = authService.getJastiperIdByUsername(username);
        assertEquals(jastiperId, result);
    }

    @Test
    void testGetJastiperIdByUsername_BannedAccount_ThrowsForbidden() {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", jastiperId.toString());
        mockData.put("status", "BANNED");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", mockData);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void testGetJastiperIdByUsername_InactiveAccount_ThrowsForbidden() {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", jastiperId.toString());
        mockData.put("is_active", false);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", mockData);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void getJastiperId_Fail_InvalidFormat() {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("wrong_key", jastiperId.toString());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", mockData);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getJastiperId_Fail_ConnectionError() {
        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenThrow(new RuntimeException("Connection Refused"));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void getJastiperId_Fail_NotFound() {
        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenThrow(mock(HttpClientErrorException.NotFound.class));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetJastiperProfile_Success() {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("username", "testuser");
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", mockData);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        Map<String, Object> result = authService.getJastiperProfile(jastiperId);
        assertNotNull(result);
        assertEquals("testuser", result.get("username"));
    }

    @Test
    void testGetJastiperProfile_FailOrNull() {
        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(null);
        Map<String, Object> result = authService.getJastiperProfile(jastiperId);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetJastiperProfile_ReturnsSuccessFalse() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", false);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        Map<String, Object> result = authService.getJastiperProfile(jastiperId);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetJastiperProfile_ThrowsException() {
        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenThrow(new RuntimeException("Timeout"));
        Map<String, Object> result = authService.getJastiperProfile(jastiperId);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetJastiperIdByUsername_NullResponse() {
        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetJastiperIdByUsername_SuccessIsFalse() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", false);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetJastiperIdByUsername_MissingDataKey() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetJastiperIdByUsername_ExtractIdReturnsNull() {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("unknown_key_format", jastiperId.toString());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("data", mockData);

        when(restTemplate.getForObject(anyString(), eq(Map.class), any(Object[].class))).thenReturn(mockResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername(username));
        assertEquals(NOT_FOUND, exception.getStatusCode());
    }
}