package com.diegovilla.pos.core.security.ratelimit;

import com.diegovilla.pos.core.errors.factories.ErrorResponseFactory;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Filter that applies token-bucket rate limiting to incoming HTTP requests.
 *
 * <p>Identifies clients by their authenticated user identifier or origin IP address. Emits
 * informational {@code X-RateLimit-Remaining} headers on successful requests, or returns HTTP 429
 * Too Many Requests with retry headers when limits are exceeded.
 *
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/auth/login";

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final ErrorResponseFactory errorResponseFactory;

    /**
     * Evaluates client token consumption and continues filter chain or halts with HTTP 429.
     *
     * @param request current HTTP request.
     * @param response current HTTP response.
     * @param filterChain servlet filter chain.
     * @throws ServletException in case of servlet execution errors.
     * @throws IOException in case of I/O read or write errors.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip when rate limiting is disabled by configuration
        if (!properties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Resolve the client key (authenticated user or remote IP); login has its own bucket
        boolean isLogin = request.getRequestURI().endsWith(LOGIN_PATH);
        String clientKey = (isLogin ? "login:" : "") + resolveClientKey(request);

        // 2. Try to consume 1 token
        ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnProbe(clientKey, isLogin);

        if (probe.isConsumed()) {
            // Allowed: expose the remaining tokens
            response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Limit exceeded: HTTP 429 Too Many Requests
            long waitForRefillSeconds =
                    TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

            log.warn(
                    "Rate limit exceeded for clientKey={}. Retry after {}s",
                    clientKey,
                    waitForRefillSeconds);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("Retry-After", String.valueOf(waitForRefillSeconds));

            // Standard error body built with the shared factory
            var errorResponse =
                    errorResponseFactory
                            .build(
                                    HttpStatus.TOO_MANY_REQUESTS,
                                    "You have exceeded your request limit. Try again in "
                                            + waitForRefillSeconds
                                            + " seconds.",
                                    List.of())
                            .getBody();

            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    /**
     * Resolves the unique identifier representing the client (authenticated user ID or client IP).
     *
     * @param request current HTTP servlet request.
     * @return a formatted key string such as {@code user:<uuid>} or {@code ip:<address>}.
     */
    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }

        // Client IP behind proxies / load balancers (Render, Nginx, Cloudflare)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
