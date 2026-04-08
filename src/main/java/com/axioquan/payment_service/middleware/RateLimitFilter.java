// ============================================
// File: RateLimitFilter.java
// Location: src/main/java/com/axioquan/payment_service/middleware/
// Purpose: Fixed-window rate limiting using Google Guava Cache + AtomicInteger
// ============================================

package com.axioquan.payment_service.middleware;

import com.axioquan.payment_service.errors.RateLimitExceededException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter using a fixed-window counter (Guava Cache + AtomicInteger).
 *
 * Supports different rate limits for different endpoints:
 * - /api/v1/payments/initialize: 5 requests/minute per authenticated user
 * - /api/v1/webhooks/paystack: 100 requests/minute per IP address
 *
 * Each counter expires after exactly 1 minute (expireAfterWrite), guaranteeing
 * a clean window reset regardless of ongoing traffic.
 *
 * NOTE: Counters are in-memory — suitable for single-instance deployments.
 * For distributed deployments, use a Redis-backed rate limiter instead.
 */
@Slf4j
@Component
public class RateLimitFilter implements Filter {

    // Fixed-window request counters per key (user or IP).
    // expireAfterWrite guarantees the window resets after exactly 1 minute,
    // regardless of whether the entry is accessed during that period.
    private final Cache<String, AtomicInteger> requestCounts = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Value("${app.ratelimit.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Only process HTTP requests
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Check rate limits based on endpoint
            if (rateLimitEnabled && shouldApplyRateLimit(httpRequest)) {
                checkRateLimit(httpRequest, httpResponse);
            }

            // Continue filter chain
            chain.doFilter(request, response);

        } catch (RateLimitExceededException e) {
            // Handle rate limit exceeded - return 429 Too Many Requests
            handleRateLimitExceeded(httpResponse, e);
        }
    }

    /**
     * Determine if this request should have rate limiting applied.
     *
     * @param request The HTTP request
     * @return true if rate limiting should be applied
     */
    private boolean shouldApplyRateLimit(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Apply rate limiting to payment initialization endpoint
        if (path.contains("/api/v1/payments/initialize") || path.contains("/v1/payments/initialize")) {
            return true;
        }

        // Apply rate limiting to webhook endpoint
        if (path.contains("/api/v1/webhooks/paystack") || path.contains("/v1/webhooks/paystack")) {
            return true;
        }

        return false;
    }

    /**
     * Check if request exceeds rate limit.
     *
     * @param request The HTTP request
     * @param response The HTTP response (for adding rate limit headers)
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    private void checkRateLimit(HttpServletRequest request, HttpServletResponse response)
            throws RateLimitExceededException {

        String path = request.getRequestURI();
        String limitKey;
        long limitsPerMinute;

        // Determine rate limit based on endpoint
        if (path.contains("/api/v1/payments/initialize") || path.contains("/v1/payments/initialize")) {
            // Payment initialization: 5 requests per minute per user
            limitKey = getLimitKeyForPaymentInitialize(request);
            limitsPerMinute = 5;
        } else if (path.contains("/api/v1/webhooks/paystack") || path.contains("/v1/webhooks/paystack")) {
            // Webhook: 100 requests per minute per IP
            limitKey = getLimitKeyForWebhook(request);
            limitsPerMinute = 100;
        } else {
            // Unknown endpoint - don't apply rate limiting
            return;
        }

        try {
            // Increment the fixed-window counter for this key.
            // The cache entry (and thus the counter) expires after exactly 1 minute
            // via expireAfterWrite, giving a clean reset every 60 seconds.
            AtomicInteger counter = requestCounts.get(limitKey, AtomicInteger::new);
            int currentCount = counter.incrementAndGet();

            if (currentCount > limitsPerMinute) {
                int retryAfterSeconds = 60;

                log.warn("Rate limit exceeded for key: {} ({}/{} per min)",
                        limitKey, currentCount, limitsPerMinute);

                response.setHeader("X-RateLimit-Limit", String.valueOf(limitsPerMinute));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + retryAfterSeconds * 1000L));
                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

                throw new RateLimitExceededException(
                        "Rate limit exceeded. Maximum " + limitsPerMinute + " requests per minute. " +
                                "Please try again in " + retryAfterSeconds + " second(s).",
                        retryAfterSeconds,
                        limitKey,
                        limitsPerMinute,
                        60
                );
            }

            // Request is within the allowed window
            long remaining = Math.max(0, limitsPerMinute - currentCount);
            response.setHeader("X-RateLimit-Limit", String.valueOf(limitsPerMinute));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000L));

            log.debug("Rate limit check passed for key: {} ({}/{} per min)", limitKey, currentCount, limitsPerMinute);

        } catch (ExecutionException e) {
            // Allow the request through rather than blocking on rate-limiter failure
            log.error("Error checking rate limit for key: {}", limitKey, e);
        }
    }

    /**
     * Get the limit key for payment initialization endpoint.
     * Uses authenticated user ID to track per-user rates.
     *
     * @param request The HTTP request
     * @return The limit key (user ID or fallback)
     */
    private String getLimitKeyForPaymentInitialize(HttpServletRequest request) {
        // Try to get user ID from JWT token in Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Extract user ID from token (first 16 chars of token hash as unique identifier)
            return "user:" + Math.abs(token.hashCode()) % 1000000;
        }

        // Fallback to IP address if no auth present
        return "ip:" + getClientIp(request);
    }

    /**
     * Get the limit key for webhook endpoint.
     * Uses client IP address to track per-IP rates.
     *
     * @param request The HTTP request
     * @return The limit key (IP address)
     */
    private String getLimitKeyForWebhook(HttpServletRequest request) {
        return "webhook-ip:" + getClientIp(request);
    }

    /**
     * Get client IP address from request.
     * Checks X-Forwarded-For header (for proxied requests) first.
     *
     * @param request The HTTP request
     * @return The client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        // Check X-Forwarded-For header (for reverse proxies like Render)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header (for some reverse proxies)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to direct remote address
        return Optional.ofNullable(request.getRemoteAddr()).orElse("0.0.0.0");
    }

    /**
     * Handle rate limit exceeded response.
     *
     * @param response The HTTP response
     * @param exception The RateLimitExceededException
     */
    private void handleRateLimitExceeded(HttpServletResponse response, RateLimitExceededException exception)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
        response.setContentType("application/json");
        response.setHeader("Retry-After", String.valueOf(exception.getRetryAfterSeconds()));

        // Write JSON error response
        String errorJson = String.format(
                "{\"status\":%d,\"message\":\"%s\",\"error\":\"RATE_LIMIT_EXCEEDED\",\"retryAfterSeconds\":%d}",
                HttpStatus.TOO_MANY_REQUESTS.value(),
                exception.getMessage().replace("\"", "\\\""),
                exception.getRetryAfterSeconds()
        );

        response.getWriter().write(errorJson);
        response.getWriter().flush();

        log.warn("Rate limit response sent: {} (retry after: {}s)",
                exception.getMessage(), exception.getRetryAfterSeconds());
    }
}
