package com.diegovilla.pos.core.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties(true, 2, 2, 1, 1);
        rateLimitService = new RateLimitService(properties);
    }

    @Test
    @DisplayName("Should allow consumption within limit")
    void shouldAllowConsumptionWithinLimit() {
        ConsumptionProbe probe1 = rateLimitService.tryConsumeAndReturnProbe("client-1", false);
        assertThat(probe1.isConsumed()).isTrue();
        assertThat(probe1.getRemainingTokens()).isEqualTo(1);

        ConsumptionProbe probe2 = rateLimitService.tryConsumeAndReturnProbe("client-1", false);
        assertThat(probe2.isConsumed()).isTrue();
        assertThat(probe2.getRemainingTokens()).isZero();
    }

    @Test
    @DisplayName("Should reject consumption when rate limit exceeded")
    void shouldRejectWhenLimitExceeded() {
        rateLimitService.tryConsumeAndReturnProbe("client-2", false);
        rateLimitService.tryConsumeAndReturnProbe("client-2", false);

        ConsumptionProbe probe3 = rateLimitService.tryConsumeAndReturnProbe("client-2", false);
        assertThat(probe3.isConsumed()).isFalse();
        assertThat(probe3.getNanosToWaitForRefill()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should track different clients separately")
    void shouldTrackDifferentClientsSeparately() {
        ConsumptionProbe clientA = rateLimitService.tryConsumeAndReturnProbe("client-a", false);
        ConsumptionProbe clientB = rateLimitService.tryConsumeAndReturnProbe("client-b", false);

        assertThat(clientA.isConsumed()).isTrue();
        assertThat(clientB.isConsumed()).isTrue();
        assertThat(clientA.getRemainingTokens()).isEqualTo(1);
        assertThat(clientB.getRemainingTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should apply the stricter login limit when strict is true")
    void shouldApplyStricterLoginLimit() {
        ConsumptionProbe first = rateLimitService.tryConsumeAndReturnProbe("login:ip-1", true);
        ConsumptionProbe second = rateLimitService.tryConsumeAndReturnProbe("login:ip-1", true);

        assertThat(first.isConsumed()).isTrue();
        assertThat(second.isConsumed()).isFalse();
    }
}
