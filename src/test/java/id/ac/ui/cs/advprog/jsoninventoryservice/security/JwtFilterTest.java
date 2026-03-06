package id.ac.ui.cs.advprog.jsoninventoryservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
}