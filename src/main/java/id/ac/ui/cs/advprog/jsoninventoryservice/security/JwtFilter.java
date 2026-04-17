package id.ac.ui.cs.advprog.jsoninventoryservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    String accountId = jwtUtil.getAccountIdFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);

                    if (accountId != null) {
                        UUID uuidAccountId = UUID.fromString(accountId);
                        request.setAttribute("userRole", role);
                        if ("ADMIN".equals(role)) {
                            request.setAttribute("adminId", uuidAccountId);
                        } else {
                            request.setAttribute("jastiperId", uuidAccountId);
                        }

                        if (role != null) {
                            String authorityRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(authorityRole));
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(accountId, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        filterChain.doFilter(request, response);
    }
}