
// ============================================
// File 9: PaystackHttpClient.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/PaystackHttpClient.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack;

import com.axioquan.payment_service.config.PaystackProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Component
@Slf4j
public class PaystackHttpClient {

    private final PaystackProperties properties;
    private RestTemplate restTemplate;

    public PaystackHttpClient(PaystackProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        this.restTemplate = createRestTemplate();
        log.info("✅ Paystack HTTP Client initialized");
    }

    private RestTemplate createRestTemplate() {

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(50);
        connectionManager.setDefaultMaxPerRoute(20);

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(properties.getConnectionTimeout()))
                .build();

        connectionManager.setDefaultConnectionConfig(connectionConfig);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(properties.getConnectionTimeout()))
                .setResponseTimeout(Timeout.ofMilliseconds(properties.getReadTimeout()))
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }

    /**
     * 🔥 REQUIRED for PaystackClient
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}