package com.andrija.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) konfiguracija.
 *
 * Problem koji rešavamo:
 * Browser blokira zahteve iz jednog origin-a (http://localhost:5173 — React/Vite)
 * ka drugom origin-u (http://localhost:8080 — Spring Boot).
 * Ovo je browser sigurnosna politika — "Same-Origin Policy".
 *
 * VAŽNO — Spring Security 6:
 * U Spring Security 6, CORS mora biti registrovan UNUTAR Security filter chain-a.
 * Zato koristimo CorsConfigurationSource @Bean koji SecurityConfig preuzima
 * sa .cors(Customizer.withDefaults()).
 * Stari pristup sa standalone CorsFilter @Bean ne funkcioniše ispravno jer
 * OPTIONS preflight zahtevi bivaju blokirani pre nego što CORS filter ima šansu
 * da ih obradi.
 *
 * Za produkciju: zameniti localhost URL-ove sa pravim domenom.
 */
@Configuration
public class CorsConfig {

    /**
     * CorsConfigurationSource — definiše CORS pravila.
     *
     * SecurityConfig ga koristi kroz:
     * .cors(Customizer.withDefaults())
     * Spring Security automatski pronalazi ovaj @Bean po tipu.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Dozvoljeni origini — React frontend (Vite defaultno koristi port 5173 ili 5174)
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5174"
        ));

        // Dozvoljene HTTP metode — uključujemo OPTIONS za CORS preflight zahteve
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Dozvoljeni headeri — uključujemo Authorization (za JWT token)
        config.setAllowedHeaders(List.of("*"));

        // Dozvoljavamo slanje Authorization headera
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Primenjujemo CORS konfiguraciju na sve putanje
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
