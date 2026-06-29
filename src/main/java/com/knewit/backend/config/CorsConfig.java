package com.knewit.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 1. Fix: Matches ALL application routes and endpoints
                        .allowedOriginPatterns(
                                "http://localhost:[*]",      // Matches any port on localhost
                                "http://127.0.0.1:[*]"       // Matches any port on local loopback
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 2. Fix: Explicitly allowed REST methods
                        .allowedHeaders("Authorization", "Content-Type", "X-Requested-With") // 3. Fix: Allows headers, including JWT tokens
                        .allowCredentials(true); // Allows cookies/auth headers to pass securely
            }
        };
    }
}
