package id.ac.ui.cs.advprog.jsoninventoryservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    String accountId = jwtUtil.getAccountIdFromToken(token);
                    if (accountId != null) {
                        request.setAttribute("jastiperId", UUID.fromString(accountId));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }
}