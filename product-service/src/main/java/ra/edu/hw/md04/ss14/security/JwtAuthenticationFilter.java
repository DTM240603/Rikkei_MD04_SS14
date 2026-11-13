package ra.edu.hw.md04.ss14.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtils.parseClaims(header.substring(7));
                Collection<SimpleGrantedAuthority> authorities = authoritiesFrom(claims);
                var authentication = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private Collection<SimpleGrantedAuthority> authoritiesFrom(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (!(rolesClaim instanceof List<?> roles)) {
            return List.of();
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Object role : roles) {
            if (role instanceof String value && !value.isBlank()) {
                String authority = value.startsWith("ROLE_") ? value : "ROLE_" + value;
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        return authorities;
    }
}
