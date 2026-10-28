package com.diegovilla.pos.core.security.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Service managing client token buckets for rate limiting using Bucket4j.
 *
 * <p>Maintains an in-memory concurrent map of client buckets configured according to {@link
 * RateLimitProperties} and tracks probe consumption state.
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitService {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Attempts to consume 1 token for the specified client key and returns consumption probe
     * metrics.
     *
     * @param key unique identifier representing the client (e.g. user ID or remote IP).
     * @param strict {@code true} to use the stricter login limit.
     * @return a {@link ConsumptionProbe} containing token consumption status and nanos until
     *     refill.
     */
    public ConsumptionProbe tryConsumeAndReturnProbe(String key, boolean strict) {
        Bucket bucket = cache.computeIfAbsent(key, k -> createNewBucket(strict));
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    /**
     * Creates a new Bucket configured with bandwidth capacity and refill interval.
     *
     * @param strict {@code true} to use the stricter login limit.
     * @return a newly initialized {@link Bucket} instance.
     */
    private Bucket createNewBucket(boolean strict) {
        int capacity = strict ? properties.loginCapacity() : properties.capacity();
        int refillTokens = strict ? properties.loginCapacity() : properties.refillTokens();

        return Bucket.builder()
                .addLimit(
                        limit ->
                                limit.capacity(capacity)
                                        .refillIntervally(
                                                refillTokens,
                                                Duration.ofMinutes(properties.refillMinutes())))
                .build();
    }
}
