package com.andrija.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT filter koji se izvršava jednom po svakom HTTP zahtevu (OncePerRequestFilter).
 *
 * Mesto u Spring Security lancu filtera:
 * Request → [JwtAuthenticationFilter] → [UsernamePasswordAuthenticationFilter] → Controller
 *
 * Naš filter se registruje ISPRED UsernamePasswordAuthenticationFilter u SecurityConfig-u.
 *
 * Šta ovaj filter radi (korak po korak):
 * 1. Čita "Authorization" header iz HTTP zahteva
 * 2. Proverava da li počinje sa "Bearer " (standardni JWT format)
 * 3. Izvlači JWT token (uklanja "Bearer " prefiks)
 * 4. Čita email iz JWT tokena (JwtService.extractEmail)
 * 5. Učitava korisnika iz baze (CustomUserDetailsService)
 * 6. Validira token (potpis + datum isteka)
 * 7. Postavlja korisnika u SecurityContext — Spring Security sada "zna" ko je ulogovan
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Korak 1: Čitamo Authorization header
        final String authorizationHeader = request.getHeader("Authorization");

        // Korak 2: Ako nema headera ili ne počinje sa "Bearer " — preskačemo (nema JWT tokena)
        // Zahtev se i dalje prosleđuje — Spring Security će ga blokirati ako endpoint zahteva auth
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Korak 3: Izvlačimo token (uklanjamo "Bearer " — 7 karaktera)
        final String jwtToken = authorizationHeader.substring(7);

        // Korak 4: Čitamo email iz tokena
        final String email;
        try {
            email = jwtService.extractEmail(jwtToken);
        } catch (Exception e) {
            // Token je neispravan (loš potpis, format...) — preskačemo autentifikaciju
            filterChain.doFilter(request, response);
            return;
        }

        // Korak 5: Ako imamo email i korisnik još nije autentifikovan u ovom zahtevu
        // (SecurityContextHolder.getContext().getAuthentication() == null)
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Učitavamo korisnika iz baze
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Korak 6: Validiramo token
            if (jwtService.isTokenValid(jwtToken, userDetails)) {

                // Korak 7: Kreiramo Authentication objekat i postavljamo ga u SecurityContext
                // UsernamePasswordAuthenticationToken(principal, credentials, authorities)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,                       // credentials = null (ne trebaju nam više)
                        userDetails.getAuthorities() // ROLE_ADMIN ili ROLE_CUSTOMER
                );

                // Dodajemo detalje zahteva (IP adresa, session ID...)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Postavljamo u SecurityContext — od sada Spring Security "zna" ko je ulogovan
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Prosleđujemo zahtev sledećem filteru/kontroleru
        filterChain.doFilter(request, response);
    }
}
