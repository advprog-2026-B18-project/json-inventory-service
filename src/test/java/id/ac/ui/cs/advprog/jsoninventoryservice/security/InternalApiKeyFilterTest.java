package id.ac.ui.cs.advprog.jsoninventoryservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalApiKeyFilterTest {
    @InjectMocks
    private InternalApiKeyFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "internalApiKey", "test-secret-key");
    }

    @Test
    void testDoFilterInternal_NonInternalPath_Bypass() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/products");
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InternalPath_ValidKey_Allowed() throws Exception {
        when(request.getRequestURI()).thenReturn("/internal/products/123/reserve");
        when(request.getHeader("X-Service-Key")).thenReturn("test-secret-key");
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InternalPath_InvalidKey_Blocked() throws Exception {
        when(request.getRequestURI()).thenReturn("/internal/products/123/reserve");
        when(request.getHeader("X-Service-Key")).thenReturn("wrong-key");
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
        filter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InternalPath_NullKey_Blocked() throws Exception {
        when(request.getRequestURI()).thenReturn("/internal/products/123/reserve");
        when(request.getHeader("X-Service-Key")).thenReturn(null);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
        filter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }
}