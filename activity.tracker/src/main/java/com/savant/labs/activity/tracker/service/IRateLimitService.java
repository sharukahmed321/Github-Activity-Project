package com.savant.labs.activity.tracker.service;

import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

public interface IRateLimitService {
    Mono<Void> checkRateLimit();
    void updateRateLimit(int remaining, long reset);
    int getRemainingRequests();
    LocalDateTime getResetTime();
}
