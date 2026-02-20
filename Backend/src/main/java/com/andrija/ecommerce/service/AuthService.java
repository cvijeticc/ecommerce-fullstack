package com.andrija.ecommerce.service;

import com.andrija.ecommerce.dto.LoginRequest;
import com.andrija.ecommerce.dto.LoginResponse;
import com.andrija.ecommerce.dto.RegisterRequest;
import com.andrija.ecommerce.entity.User;
import com.andrija.ecommerce.enums.Role;
import com.andrija.ecommerce.exception.DuplicateEmailException;
import com.andrija.ecommerce.repository.UserRepository;
import com.andrija.ecommerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servis koji rukuje autentifikacijom korisnika.
 *
 * Dve operacije:
 * 1. register() — kreira novog korisnika u bazi
 * 2. login()    — autentifikuje korisnika i vraća JWT token
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder iz SecurityConfig
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // iz SecurityConfig

    /**
     * Registruje novog korisnika.
     *
     * Tok:
     * 1. Proverava da li email već postoji (409 Conflict ako postoji)
     * 2. Hash-uje lozinku BCrypt algoritmom — NIKADA ne čuvamo plain text!
     * 3. Kreira User entitet i čuva ga u bazi
     * 4. Generiše JWT token za novokreiranog korisnika
     * 5. Vraća LoginResponse sa tokenom
     *
     * Novi korisnik uvek dobija CUSTOMER role.
     * ADMIN role se dodeljuje ručno direktno u bazi (ili posebnim admin endpointom).
     *
     * @param request DTO sa podacima za registraciju
     * @return LoginResponse sa JWT tokenom
     */
    public LoginResponse register(RegisterRequest request) {
        // Korak 1: Proveriti jedinstvenost emaila
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(
                "Korisnik sa emailom '" + request.email() + "' već postoji"
            );
        }

        // Korak 2 & 3: Kreiranje i čuvanje korisnika
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // BCrypt hash!
                .role(Role.CUSTOMER) // svi novi korisnici su CUSTOMER
                .build();

        userRepository.save(user);

        // Korak 4 & 5: Generisanje tokena i vraćanje odgovora
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getEmail(), user.getRole().name());
    }

    /**
     * Autentifikuje korisnika i vraća JWT token.
     *
     * Tok:
     * 1. AuthenticationManager.authenticate() poziva CustomUserDetailsService.loadUserByUsername()
     * 2. Spring Security hash-uje primljenu lozinku i poredi sa hash-om iz baze
     * 3. Ako se ne podudaraju — baca BadCredentialsException (401)
     * 4. Ako je OK — učitavamo korisnika iz baze i generišemo JWT token
     *
     * @param request DTO sa emailom i lozinkom
     * @return LoginResponse sa JWT tokenom, emailom i role-om
     */
    public LoginResponse login(LoginRequest request) {
        // Korak 1 & 2: Spring Security proverava email i lozinku
        // Ako su pogrešni — automatski baca AuthenticationException (401)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),    // username (naš email)
                request.password()  // plain text lozinka — Spring je automatski hash-uje i poredi
            )
        );

        // Korak 3: Ako smo stigli ovde, autentifikacija je uspela — učitavamo korisnika
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(); // ne može se desiti jer authenticate() nije bacio grešku

        // Korak 4: Generišemo token i vraćamo odgovor
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getEmail(), user.getRole().name());
    }
}
