// ============================================
// File: RateLimitExceededException.java
// Location: src/main/java/com/axioquan/payment_service/errors/
// Purpose: Thrown when rate limit is exceeded
// ============================================

package com.axioquan.payment_service.errors;

import lombok.Getter;

/**
 * Exception thrown when a rate limit has been exceeded.
 * 
 * The client should retry after the specified retryAfter seconds.
 */
@Getter
public class RateLimitExceededException extends RuntimeException {

    private final int retryAfterSeconds;
    private final String limitKey;
    private final long requestsLimit;
    private final long durationSeconds;

    /**
     * Create a new RateLimitExceededException
     *
     * @param message The error message
     * @param retryAfterSeconds Seconds to wait before retrying
     * @param limitKey The key that was rate limited (e.g., user ID or IP address)
     * @param requestsLimit The maximum number of requests allowed
     * @param durationSeconds The duration in seconds for the limit
     */
    public RateLimitExceededException(
            String message,
            int retryAfterSeconds,
            String limitKey,
            long requestsLimit,
            long durationSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limitKey = limitKey;
        this.requestsLimit = requestsLimit;
        this.durationSeconds = durationSeconds;
    }

    public RateLimitExceededException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limitKey = "unknown";
        this.requestsLimit = 0;
        this.durationSeconds = 0;
    }
}
