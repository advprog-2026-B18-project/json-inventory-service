package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"unchecked", "rawtypes"})
class AuthIntegrationServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthIntegrationServiceImpl authService;

    private String expectedId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "authServiceUrl", "http://localhost:8080/api/auth");
        ReflectionTestUtils.setField(authService, "restTemplate", restTemplate);
        expectedId = UUID.randomUUID().toString();
    }

    @Test
    void getJastiperId_Success_WithId() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("id", expectedId);
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        UUID result = authService.getJastiperIdByUsername("user1");
        assertEquals(UUID.fromString(expectedId), result);
    }

    @Test
    void getJastiperId_Success_WithUserId() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("userId", expectedId);
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        UUID result = authService.getJastiperIdByUsername("user1");
        assertEquals(UUID.fromString(expectedId), result);
    }

    @Test
    void getJastiperId_Success_WithUser_id() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("user_id", expectedId);
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        UUID result = authService.getJastiperIdByUsername("user1");
        assertEquals(UUID.fromString(expectedId), result);
    }

    @Test
    void getJastiperId_Success_WithAccountId() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("accountId", expectedId);
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        UUID result = authService.getJastiperIdByUsername("user1");
        assertEquals(UUID.fromString(expectedId), result);
    }

    @Test
    void getJastiperId_Fail_NotFound() {
        HttpClientErrorException.NotFound notFoundException = Mockito.mock(HttpClientErrorException.NotFound.class);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(notFoundException);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("unknown"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetJastiperIdByUsername_ActiveAccount_Success() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("id", expectedId);
        dataMap.put("status", "ACTIVE");
        dataMap.put("is_active", true);
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        UUID result = authService.getJastiperIdByUsername("active_user");
        assertEquals(UUID.fromString(expectedId), result);
    }

    @Test
    void testGetJastiperIdByUsername_MissingStatusAndIsActiveKeys_Success() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("id", expectedId);
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        UUID result = authService.getJastiperIdByUsername("normal_user");
        assertEquals(UUID.fromString(expectedId), result);
    }

    @Test
    void testGetJastiperIdByUsername_BannedAccount_ThrowsForbidden() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("id", expectedId);
        dataMap.put("status", "BANNED");
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("banned_user"));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void testGetJastiperIdByUsername_InactiveAccount_ThrowsForbidden() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("id", expectedId);
        dataMap.put("is_active", false);
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("inactive_user"));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void getJastiperId_Fail_ResponseNull() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("user1"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid response format"));
    }

    @Test
    void getJastiperId_Fail_SuccessFalse() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", false);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("user1"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getJastiperId_Fail_MissingData() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("user1"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getJastiperId_Fail_MissingIdKey() {
        Map rawResponse = new HashMap();
        rawResponse.put("success", true);
        Map dataMap = new HashMap();
        dataMap.put("name", "Nahla");
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("user1"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getJastiperId_Fail_ConnectionError() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("Connection Timeout"));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getJastiperIdByUsername("user1"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void testGetJastiperProfile_FailOrNull() {
        UUID userId = UUID.randomUUID();
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Simulated Timeout or Error"));
        Map<String, Object> result = authService.getJastiperProfile(userId);
        assertNull(result);
    }

    @Test
    void testGetJastiperProfile_Success() {
        Map<String, Object> rawResponse = new HashMap<>();
        rawResponse.put("success", true);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("username", "jastiper123");
        rawResponse.put("data", dataMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(rawResponse);
        Map<String, Object> result = authService.getJastiperProfile(UUID.randomUUID());
        assertNotNull(result);
        assertEquals("jastiper123", result.get("username"));
    }

    @Test
    void testGetJastiperProfile_ReturnsSuccessFalse() {
        java.util.Map<String, Object> rawResponse = new java.util.HashMap<>();
        rawResponse.put("success", false);
        when(restTemplate.getForObject(anyString(), eq(java.util.Map.class))).thenReturn(rawResponse);
        java.util.Map<String, Object> result = authService.getJastiperProfile(UUID.randomUUID());
        assertNull(result);
    }

    @Test
    void testGetJastiperProfile_ThrowsException() {
        when(restTemplate.getForObject(anyString(), eq(java.util.Map.class))).thenThrow(new RuntimeException("Connection Timeout"));
        java.util.Map<String, Object> result = authService.getJastiperProfile(UUID.randomUUID());
        assertNull(result);
    }
}