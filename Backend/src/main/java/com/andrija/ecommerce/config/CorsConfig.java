package com.andrija.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) konfiguracija.
 *
 * Problem koji rešavamo:
 * Browser blokira zahteve iz jednog origin-a (http://localhost:3000 — React)
 * ka drugom origin-u (http://localhost:8080 — Spring Boot).
 * Ovo je browser sigurnosna politika — "Same-Origin Policy".
 *
 * Rešenje: server mora da kaže "dozvoljavam zahteve sa http://localhost:3000".
 * To se radi kroz CORS headere u HTTP odgovoru.
 *
 * Za produkciju: zameniti "http://localhost:3000" sa pravim domenom.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Dozvoljeni origin — React frontend na lokalnoj mašini
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // Dozvoljene HTTP metode
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Dozvoljeni headeri — uključujemo Authorization (za JWT token)
        config.setAllowedHeaders(List.of("*"));

        // Dozvoljavamo slanje Cookie-ja i Authorization headera
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Primenjujemo CORS konfiguraciju na sve putanje
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
