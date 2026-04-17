package id.ac.ui.cs.advprog.jsoninventoryservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {
    @Value("${INTERNAL_SERVICE_KEY:secret-service-key}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/internal/")) {
            String requestKey = request.getHeader("X-Service-Key");
            if (requestKey == null || !requestKey.equals(internalApiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Unauthorized: Invalid or missing Internal API Key\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}