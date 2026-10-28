package com.diegovilla.pos.core.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security filter that intercepts HTTP requests to validate JWT Bearer tokens.
 *
 * <p>Extracts the {@code Authorization} header, verifies the access token via {@link JwtService}
 * and populates the {@link SecurityContextHolder} with the authenticated cashier ID as principal.
 * There are no roles, so no authorities are granted: being authenticated is enough.
 *
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    /**
     * Filters incoming requests to authenticate cashiers via JWT Bearer tokens.
     *
     * @param request the current HTTP servlet request.
     * @param response the current HTTP servlet response.
     * @param filterChain execution filter chain to proceed if authenticated or token absent.
     * @throws ServletException in case of general servlet processing errors.
     * @throws IOException in case of I/O errors while reading or writing to streams.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(BEARER_PREFIX.length());

        if (!jwtService.isValid(token, true)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = jwtService.extractSubject(token, true);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
