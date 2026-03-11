package com.andrija.ecommerce.service;

import com.andrija.ecommerce.dto.LoginRequest;
import com.andrija.ecommerce.dto.LoginResponse;
import com.andrija.ecommerce.dto.RegisterRequest;
import com.andrija.ecommerce.entity.User;
import com.andrija.ecommerce.enums.Role;
import com.andrija.ecommerce.exception.DuplicateEmailException;
import com.andrija.ecommerce.repository.UserRepository;
import com.andrija.ecommerce.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit testovi za AuthService.
 *
 * Šta testiramo:
 * 1. register — uspešna registracija (čuva korisnika, vraća token)
 * 2. register — greška kada email već postoji
 * 3. login    — uspešan login (vraća token)
 * 4. login    — greška sa pogrešnom lozinkom
 *
 * @ExtendWith(MockitoExtension.class) — aktivira Mockito anotacije (@Mock, @InjectMocks).
 * Bez ovoga @Mock ne bi radio.
 *
 * @Mock — kreira lažni (mock) objekat. Ne zove pravi kod, mi definišemo šta vraća (when/thenReturn).
 * Koristimo ga za ZAVISNOSTI klase koju testiramo.
 *
 * @InjectMocks — kreira pravi objekat klase i UBACUJE sve @Mock zavisnosti.
 * Ovo je klasa KOJU testiramo.
 *
 * Zašto mockujemo zavisnosti?
 * - AuthService zavisi od: UserRepository (baza!), PasswordEncoder, JwtService, AuthenticationManager
 * - U unit testu NE SMEMO da koristimo pravu bazu — to je integration test
 * - Mock nam daje kontrolu: "kada neko pozove userRepository.existsByEmail(), vrati true"
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // Zavisnosti AuthService-a — sve mockujemo (nema prave baze!)
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    // Klasa koju testiramo — Mockito automatski ubacuje gornje @Mock zavisnosti
    @InjectMocks
    private AuthService authService;

    // ─── Testovi za register() ────────────────────────────────────────────────

    @Test
    @DisplayName("register treba da sačuva korisnika sa hashovanom lozinkom i vrati token")
    void register_shouldSaveUserAndReturnToken() {
        // Arrange — postavljamo šta mock-ovi vraćaju
        RegisterRequest request = new RegisterRequest(
                "Marko", "Marković", "marko@test.com", "lozinka123"
        );

        // Email ne postoji u bazi
        when(userRepository.existsByEmail("marko@test.com")).thenReturn(false);
        // BCrypt hash simulacija
        when(passwordEncoder.encode("lozinka123")).thenReturn("$2a$12$hashedPassword");
        // save() vraća isti objekat koji je prosleđen (simulira JPA save)
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        // JWT token
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-xyz");

        // Act
        LoginResponse response = authService.register(request);

        // Assert — proveravamo odgovor
        assertThat(response.token()).isEqualTo("jwt-token-xyz");
        assertThat(response.email()).isEqualTo("marko@test.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");

        // Verifikujemo da je save() pozvan TAČNO JEDNOM
        verify(userRepository, times(1)).save(any(User.class));
        // Verifikujemo da je lozinka hashována
        verify(passwordEncoder, times(1)).encode("lozinka123");
    }

    @Test
    @DisplayName("register treba da baci DuplicateEmailException kada email već postoji")
    void register_shouldThrow_whenEmailAlreadyExists() {
        // Arrange — email POSTOJI u bazi
        RegisterRequest request = new RegisterRequest(
                "Marko", "Marković", "marko@test.com", "lozinka123"
        );
        when(userRepository.existsByEmail("marko@test.com")).thenReturn(true);

        // Act + Assert — assertThatThrownBy proverava da metoda BACA grešku
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("marko@test.com");

        // Verifikujemo da save() NIKAD nije pozvan — prekinuli smo pre čuvanja
        verify(userRepository, never()).save(any(User.class));
    }

    // ─── Testovi za login() ───────────────────────────────────────────────────

    @Test
    @DisplayName("login treba da vrati JWT token kada su kredencijali ispravni")
    void login_shouldReturnToken_whenCredentialsAreCorrect() {
        // Arrange
        LoginRequest request = new LoginRequest("marko@test.com", "lozinka123");

        User user = User.builder()
                .email("marko@test.com")
                .password("$2a$12$hashedPassword")
                .role(Role.CUSTOMER)
                .build();

        // authenticationManager.authenticate() — ne baca grešku = uspešna autentifikacija
        // (doNothing().when() nije potrebno jer mock po defaultu ništa ne radi)
        when(userRepository.findByEmail("marko@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token-abc");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertThat(response.token()).isEqualTo("jwt-token-abc");
        assertThat(response.email()).isEqualTo("marko@test.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("login treba da baci BadCredentialsException kada je lozinka pogrešna")
    void login_shouldThrow_whenPasswordIsWrong() {
        // Arrange — authenticationManager BACA grešku (Spring Security mehanizam)
        LoginRequest request = new LoginRequest("marko@test.com", "pogresna-lozinka");

        doThrow(new BadCredentialsException("Pogrešni kredencijali"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        // findByEmail nikad nije pozvan — prekinuli smo na authenticate()
        verify(userRepository, never()).findByEmail(any());
    }
}
