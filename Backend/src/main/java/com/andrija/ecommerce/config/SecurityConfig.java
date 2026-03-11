package com.andrija.ecommerce.config;

import com.andrija.ecommerce.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.Customizer;

/**
 * Centralna konfiguracija Spring Security-ja.
 *
 * @Configuration — označava da ova klasa definiše Spring Bean-ove
 * @EnableWebSecurity — aktivira Spring Security web sigurnost
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Definišemo pravila pristupa za svaki endpoint.
     *
     * Redosled requestMatchers je bitan — Spring Security ih primenjuje redom!
     * Specifičnija pravila moraju biti PRE generičnijih.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Gasimo CSRF — ne treba nam jer koristimo JWT (stateless), ne session cookie
            .csrf(AbstractHttpConfigurer::disable)

            // CORS — koristimo CorsConfigurationSource @Bean iz CorsConfig klase.
            // Customizer.withDefaults() znači: pronađi CorsConfigurationSource bean automatski.
            // Ovo mora biti UNUTAR Security filter chain-a da OPTIONS preflight zahtevi
            // ne budu blokirani pre autentifikacije.
            .cors(Customizer.withDefaults())

            // Definišemo pravila pristupa za endpointove
            .authorizeHttpRequests(auth -> auth

                // Swagger UI i OpenAPI dokumentacija — javno dostupni
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // Javni endpointovi — svako može, bez tokena
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                // Admin endpointovi — samo ADMIN role
                // hasRole("ADMIN") traži "ROLE_ADMIN" u authorities (prefiks "ROLE_" se dodaje automatski)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Sve ostalo — mora biti autentifikovano (ulogovan korisnik, bilo koji role)
                .anyRequest().authenticated()
            )

            // Stateless sesija — server NE čuva sesiju, svaki zahtev mora imati JWT
            // Ovo je suština JWT autentifikacije!
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Registrujemo naš AuthenticationProvider (BCrypt + UserDetailsService)
            .authenticationProvider(authenticationProvider())

            // Dodajemo naš JWT filter ISPRED Spring Security UsernamePasswordAuthenticationFilter-a
            // Redosled je bitan: JWT filter mora da "identifikuje" korisnika pre Security check-a
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationProvider — definiše HOW Spring Security proverava kredencijale.
     * DaoAuthenticationProvider: uzima UserDetails iz baze i poredi password-e.
     * - userDetailsService: odakle učitavamo korisnika (iz baze, po email-u)
     * - passwordEncoder: kako poredimo lozinke (BCrypt hashing)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager — "ulaz" za programatsku autentifikaciju.
     * Koristimo ga u AuthService.login() kada pozivamo authenticate().
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCryptPasswordEncoder — hash algoritam za lozinke.
     *
     * BCrypt automatski dodaje "salt" (nasumični niz) svakom hash-u,
     * pa isti password svaki put daje drugačiji hash.
     * Ovo znači: ako dva korisnika imaju istu lozinku, hash-evi su različiti!
     *
     * strength = 10 (default) — broj rundi hash-ovanja (2^10 = 1024 iteracije)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
