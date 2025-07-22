package com.savant.labs.activity.tracker.service.impl;

import com.savant.labs.activity.tracker.exception.RateLimitExceededException;
import com.savant.labs.activity.tracker.service.IRateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class RateLimitService implements IRateLimitService {

    private final AtomicInteger remainingRequests = new AtomicInteger(5000);
    private final AtomicLong resetTime = new AtomicLong(System.currentTimeMillis() / 1000 + 3600);

    @Override
    public Mono<Void> checkRateLimit() {
        return Mono.fromRunnable(() -> {
            long currentTime = System.currentTimeMillis() / 1000;

            // Reset if the reset time has passed
            if (currentTime >= resetTime.get()) {
                remainingRequests.set(5000);
                resetTime.set(currentTime + 3600);
                log.info("Rate limit reset. Remaining requests: {}", remainingRequests.get());
            }

            int remaining = remainingRequests.decrementAndGet();

            if (remaining <= 0) {
                Duration wait = Duration.between(LocalDateTime.now(), getResetTime());
                throw new RateLimitExceededException(
                        "Rate limit exceeded. Wait " + wait.getSeconds() + " seconds",
                        getResetTime(),
                        getRemainingRequests()
                );
            }

            if (remaining <= 100) {
                log.warn("Rate limit getting low. Remaining requests: {}", remaining);
            }
        });
    }

    @Override
    public void updateRateLimit(int remaining, long reset) {
        remainingRequests.set(remaining);
        resetTime.set(reset);
        log.debug("Rate limit updated. Remaining: {}, Reset time: {}", remaining,
                LocalDateTime.ofEpochSecond(reset, 0, java.time.ZoneOffset.UTC));
    }

    @Override
    public int getRemainingRequests() {
        return remainingRequests.get();
    }

    @Override
    public LocalDateTime getResetTime() {
        return LocalDateTime.ofEpochSecond(resetTime.get(), 0, java.time.ZoneOffset.UTC);
    }
}
