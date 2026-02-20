package com.andrija.ecommerce.controller;

import com.andrija.ecommerce.dto.LoginRequest;
import com.andrija.ecommerce.dto.LoginResponse;
import com.andrija.ecommerce.dto.RegisterRequest;
import com.andrija.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler za autentifikaciju — registracija i login.
 *
 * @RestController = @Controller + @ResponseBody
 * Znači: automatski serijalizuje povratnu vrednost u JSON.
 *
 * @RequestMapping("/api/auth") — bazna putanja za sve metode u ovom kontroleru.
 *
 * Ovi endpointovi su javni — konfigurisano u SecurityConfig:
 * .requestMatchers("/api/auth/**").permitAll()
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     *
     * Registruje novog korisnika.
     *
     * @Valid — aktivira Bean Validation anotacije na RegisterRequest DTO-u.
     * Ako validacija ne prođe (npr. kratak password), Spring automatski
     * baca MethodArgumentNotValidException → GlobalExceptionHandler → 400 Bad Request.
     *
     * @return 201 Created sa JWT tokenom
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     *
     * Autentifikuje korisnika i vraća JWT token.
     *
     * @return 200 OK sa JWT tokenom, emailom i role-om
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
