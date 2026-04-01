
// ============================================
// File 13: PaystackClient.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/PaystackClient.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack;

import com.axioquan.payment_service.config.PaystackProperties;
import com.axioquan.payment_service.errors.*;
import com.axioquan.payment_service.infrastructure.paystack.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class PaystackClient {

    private final PaystackProperties properties;
    private final RestTemplate restTemplate;

    public PaystackClient(PaystackProperties properties, PaystackHttpClient httpClient) {
        this.properties = properties;
        this.restTemplate = httpClient.getRestTemplate();
    }

    // ==============================
    // INITIALIZE TRANSACTION
    // ==============================
    public PaystackInitResponse initializeTransaction(PaystackInitRequest request) {

        String url = properties.getBaseUrl() + "/transaction/initialize";

        return executeWithRetry(() -> {
            ResponseEntity<PaystackInitResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request, createHeaders()),
                    PaystackInitResponse.class
            );

            if (response.getBody() == null || !response.getBody().isStatus()) {
                throw new PaystackApiException("Initialization failed");
            }

            return response.getBody();
        });
    }

    // ==============================
    // VERIFY TRANSACTION
    // ==============================
    public PaystackVerifyResponse verifyTransaction(String reference) {

        String url = properties.getBaseUrl() + "/transaction/verify/" + reference;

        return executeWithRetry(() -> {
            ResponseEntity<PaystackVerifyResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    PaystackVerifyResponse.class
            );

            if (response.getBody() == null || !response.getBody().isStatus()) {
                throw new PaystackApiException("Verification failed");
            }

            return response.getBody();
        });
    }

    // ==============================
    // LIST BANKS
    // ==============================
    public List<PaystackBankResponse.BankData> listBanks(String country, String currency) {

        String url = properties.getBaseUrl() + "/bank";

        return executeWithRetry(() -> {
            ResponseEntity<PaystackBankResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    PaystackBankResponse.class
            );

            if (response.getBody() == null) {
                throw new PaystackApiException("Failed to fetch banks");
            }

            return response.getBody().getData();
        });
    }

    // ==============================
    // CREATE REFUND
    // ==============================
    public PaystackRefundResponse createRefund(PaystackRefundRequest request) {

        String url = properties.getBaseUrl() + "/refund";

        return executeWithRetry(() -> {
            ResponseEntity<PaystackRefundResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request, createHeaders()),
                    PaystackRefundResponse.class
            );

            if (response.getBody() == null) {
                throw new PaystackApiException("Refund failed");
            }

            return response.getBody();
        });
    }

    // ==============================
    // HEADERS
    // ==============================
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getSecretKey()); // ✅ NOW WORKS
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ==============================
    // RETRY
    // ==============================
    private <T> T executeWithRetry(ApiCall<T> call) {

        int attempts = 0;

        while (attempts <= properties.getRetryCount()) {
            try {
                return call.execute();
            } catch (Exception e) {

                if (attempts >= properties.getRetryCount()) {
                    throw new PaystackApiException("Max retries reached", e);
                }

                attempts++;
                sleep(properties.getRetryDelayMs());
            }
        }

        throw new PaystackApiException("Unexpected failure");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaystackApiException("Retry interrupted", e);
        }
    }

    @FunctionalInterface
    private interface ApiCall<T> {
        T execute();
    }
}