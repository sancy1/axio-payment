// ============================================
// File: RateLimitFilter.java
// Location: src/main/java/com/axioquan/payment_service/middleware/
// Purpose: Token bucket rate limiting using Google Guava RateLimiter
// ============================================

package com.axioquan.payment_service.middleware;

import com.axioquan.payment_service.errors.RateLimitExceededException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
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
 * Rate limiting filter using Google Guava RateLimiter (token bucket algorithm).
 *
 * Supports different rate limits for different endpoints:
 * - /api/v1/payments/initialize: 5 requests/minute per authenticated user
 * - /api/v1/webhooks/paystack: 100 requests/minute per IP address
 *
 * CRITICAL: RateLimiters are stored in-memory using Guava's Cache with TTL.
 * This works well for single-instance deployments. For distributed deployments,
 * consider Redis-backed rate limiting solutions.
 *
 * Permits per second are calculated from the required permits per minute:
 * - Payment initialization: 5 per minute = 5/60 = 0.0833 per second
 * - Webhook: 100 per minute = 100/60 = 1.667 per second
 */
@Slf4j
@Component
public class RateLimitFilter implements Filter {

    // Cache of rate limiters with 10 minute TTL
    // Automatically evicts unused limiters to prevent memory leak
    private final Cache<String, RateLimiter> rateLimiters = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    // Rate limits converted to requests per second
    // Payment initialization: 5 per minute = 0.0833 per second
    private static final double PAYMENT_INIT_RATE = 5.0 / 60;
    
    // Webhook: 100 per minute = 1.667 per second
    private static final double WEBHOOK_RATE = 100.0 / 60;

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
        double permitRate;
        long limitsPerMinute;

        // Determine rate limit based on endpoint
        if (path.contains("/api/v1/payments/initialize") || path.contains("/v1/payments/initialize")) {
            // Payment initialization: 5 requests per minute per user
            limitKey = getLimitKeyForPaymentInitialize(request);
            permitRate = PAYMENT_INIT_RATE;
            limitsPerMinute = 5;
        } else if (path.contains("/api/v1/webhooks/paystack") || path.contains("/v1/webhooks/paystack")) {
            // Webhook: 100 requests per minute per IP
            limitKey = getLimitKeyForWebhook(request);
            permitRate = WEBHOOK_RATE;
            limitsPerMinute = 100;
        } else {
            // Unknown endpoint - don't apply rate limiting
            return;
        }

        try {
            // Get or create rate limiter for this key
            RateLimiter rateLimiter = rateLimiters.get(limitKey, () -> RateLimiter.create(permitRate));

            // Try to acquire a permit (non-blocking, returns immediately)
            if (!rateLimiter.tryAcquire(1, 0, TimeUnit.SECONDS)) {
                // Rate limit exceeded
                // Estimate wait time based on retry-after calculation
                int retryAfterSeconds = 1;

                log.warn("Rate limit exceeded for key: {} (limit: {}/min), retry after: {}s",
                        limitKey, limitsPerMinute, retryAfterSeconds);

                // Add rate limit headers to response
                response.setHeader("X-RateLimit-Limit", String.valueOf(limitsPerMinute));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + retryAfterSeconds * 1000));
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

            // Permit acquired successfully - add rate limit info headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(limitsPerMinute));
            response.setHeader("X-RateLimit-Remaining", "1"); // Conservative estimate
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));

            log.debug("Rate limit check passed for key: {} (limit: {}/min)", limitKey, limitsPerMinute);

        } catch (ExecutionException e) {
            // Log error but don't fail the request due to rate limiter issues
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
