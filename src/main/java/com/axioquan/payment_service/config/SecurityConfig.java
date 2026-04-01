
// ============================================
// File: SecurityConfig.java
// Location: /src/main/java/com/axioquan/payment_service/config/SecurityConfig.java
// ============================================

package com.axioquan.payment_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/health/**", "/api/health/**").permitAll()
                .requestMatchers("/v1/auth/**", "/api/v1/auth/**").permitAll()
                .requestMatchers("/v1/webhooks/paystack", "/api/v1/webhooks/paystack").permitAll()
                .requestMatchers("/actuator/health", "/api/actuator/health").permitAll()
                
                // Protected endpoints - requires valid JWT
                .requestMatchers("/v1/payments/**", "/api/v1/payments/**").authenticated()
                .requestMatchers("/v1/enrollments/**", "/api/v1/enrollments/**").authenticated()
                .requestMatchers("/v1/notifications/**", "/api/v1/notifications/**").authenticated()
                .requestMatchers("/v1/transactions/**", "/api/v1/transactions/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.disable())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}