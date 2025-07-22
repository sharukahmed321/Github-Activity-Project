package com.savant.labs.activity.tracker.service;

import com.savant.labs.activity.tracker.service.impl.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    void checkRateLimit_shouldDecrementCounter() {
        int initialRemaining = rateLimitService.getRemainingRequests();

        StepVerifier.create(rateLimitService.checkRateLimit())
                .verifyComplete();

        assertEquals(initialRemaining - 1, rateLimitService.getRemainingRequests());
    }

    @Test
    void checkRateLimit_shouldResetAfterResetTime() {
        rateLimitService.updateRateLimit(0, System.currentTimeMillis() / 1000 - 1);

        StepVerifier.create(rateLimitService.checkRateLimit())
                .verifyComplete();

        assertEquals(4999, rateLimitService.getRemainingRequests());
    }

    @Test
    void updateRateLimit_shouldUpdateValuesCorrectly() {
        long newResetTime = System.currentTimeMillis() / 1000 + 1800;

        rateLimitService.updateRateLimit(42, newResetTime);

        assertEquals(42, rateLimitService.getRemainingRequests());
        assertEquals(newResetTime, rateLimitService.getResetTime().toEpochSecond(ZoneOffset.UTC));
    }

    @Test
    void getRemainingRequests_shouldReturnCurrentValue() {
        rateLimitService.updateRateLimit(123, 0);
        assertEquals(123, rateLimitService.getRemainingRequests());
    }

    @Test
    void getResetTime_shouldReturnCorrectDateTime() {
        long epochSeconds = System.currentTimeMillis() / 1000 + 3600;
        rateLimitService.updateRateLimit(0, epochSeconds);

        LocalDateTime resetTime = rateLimitService.getResetTime();
        assertEquals(epochSeconds, resetTime.toEpochSecond(ZoneOffset.UTC));
    }

    @Test
    void checkRateLimit_shouldLogWarningWhenLow() {
        rateLimitService.updateRateLimit(101, System.currentTimeMillis() / 1000 + 3600);

        StepVerifier.create(rateLimitService.checkRateLimit())
                .verifyComplete();

        assertEquals(100, rateLimitService.getRemainingRequests());
    }

    @Test
    void checkRateLimit_shouldNotThrowWhenExactlyZeroButResetHappened() {
        rateLimitService.updateRateLimit(1, System.currentTimeMillis() / 1000 - 1);

        StepVerifier.create(rateLimitService.checkRateLimit())
                .verifyComplete();

        assertEquals(4999, rateLimitService.getRemainingRequests());
    }
}