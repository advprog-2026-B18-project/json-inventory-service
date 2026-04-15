package id.ac.ui.cs.advprog.jsoninventoryservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        String token = "valid-token";
        String accountId = UUID.randomUUID().toString();
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(accountId);
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertEquals(UUID.fromString(accountId), request.getAttribute("jastiperId"));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertNull(request.getAttribute("jastiperId"));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoHeader() throws ServletException, IOException {
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertNull(request.getAttribute("jastiperId"));
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void testDoFilterInternal_WrongHeaderFormat() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertNull(request.getAttribute("jastiperId"));
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void testDoFilterInternal_ExceptionInValidation() throws ServletException, IOException {
        String token = "error-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Error"));
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertNull(request.getAttribute("jastiperId"));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidUuidFormat() throws ServletException, IOException {
        String token = "valid-token";
        String invalidUuid = "bukan-format-uuid";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(invalidUuid);
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertNull(request.getAttribute("jastiperId"));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ValidTokenButNoAccountId() throws ServletException, IOException {
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(null);
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertNull(request.getAttribute("jastiperId"));
        verify(filterChain, times(1)).doFilter(request, response);
    }
    @Test
    void testDoFilterInternal_BypassInternalPath() throws ServletException, IOException {
        request.setRequestURI("/internal/products/123/stock/reserve");
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ValidTokenWithRole() throws ServletException, IOException {
        String token = "valid-token";
        String accountId = UUID.randomUUID().toString();
        request.setRequestURI("/admin/products");
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(accountId);
        when(jwtUtil.getRoleFromToken(token)).thenReturn("ADMIN");
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertEquals(UUID.fromString(accountId), request.getAttribute("jastiperId"));
        assertEquals("ADMIN", request.getAttribute("userRole"));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(accountId, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
        SecurityContextHolder.clearContext();
    }
}