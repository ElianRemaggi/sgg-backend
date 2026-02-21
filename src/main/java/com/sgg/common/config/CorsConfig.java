package com.sgg.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.web-origin:http://localhost:3000}")
    private String webOrigin;

    @Value("${app.cors.additional-origins:}")
    private String additionalOrigins;

    @Bean
    public CorsFilter corsFilter() {
        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add(webOrigin);

        if (additionalOrigins != null && !additionalOrigins.isBlank()) {
            for (String origin : additionalOrigins.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    allowedOrigins.add(trimmed);
                }
            }
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
