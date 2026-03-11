package com.andrija.ecommerce.security;

import com.andrija.ecommerce.entity.User;
import com.andrija.ecommerce.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit testovi za JwtService.
 *
 * Šta testiramo:
 * 1. generateToken — da li vraća neprazan token
 * 2. extractEmail  — da li izvlači tačan email iz tokena
 * 3. isTokenValid  — da li vraca true za validan token
 * 4. isTokenValid  — da li vraca false kada token pripada drugom korisniku
 *
 * Zašto ne koristimo @SpringBootTest?
 * - @SpringBootTest pokreće CELU Spring aplikaciju (bazu, sve bean-ove) — sporo!
 * - Ovde testiramo SAMO logiku JwtService klase, ne treba nam Spring kontekst.
 * - ReflectionTestUtils.setField() ubacuje vrednosti u @Value polja bez Spring-a.
 *
 * Zašto ne koristimo @Mock za JwtService?
 * - JwtService je klasa KOJU testiramo (SUT = System Under Test).
 * - Mockujemo ZAVISNOSTI klase (ovde ih nema — JwtService nema injektovanih zavisnosti).
 */
class JwtServiceTest {

    // Klasa koju testiramo (SUT — System Under Test)
    private JwtService jwtService;

    // Test korisnik koji imitira stvarnog korisnika iz baze
    private User testUser;

    /**
     * Hex string koji simulira jwt.secret iz application.yaml.
     * Mora biti minimum 64 hex karaktera (32 bajta) za HMAC-SHA256.
     * U produkciji se čuva u application.yaml ili environment varijabli.
     */
    private static final String TEST_SECRET =
            "4a5f6e7d8c9b0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b";

    private static final long TEST_EXPIRATION = 86400000L; // 24 sata u milisekundama

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // ReflectionTestUtils.setField() — ubacujemo vrednosti u privatna @Value polja
        // bez pokretanja Spring konteksta. Imitira ono što Spring radi sa @Value.
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);

        // Pravljenje test korisnika
        testUser = User.builder()
                .email("marko@test.com")
                .password("$2a$12$hashedPassword")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("generateToken treba da vrati neprazan JWT token")
    void generateToken_shouldReturnNonEmptyToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertThat(token)
                .isNotNull()
                .isNotBlank()
                .contains("."); // JWT format: header.payload.signature
    }

    @Test
    @DisplayName("extractEmail treba da vrati tačan email iz tokena")
    void extractEmail_shouldReturnCorrectEmail() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String extractedEmail = jwtService.extractEmail(token);

        // Assert
        assertThat(extractedEmail).isEqualTo("marko@test.com");
    }

    @Test
    @DisplayName("isTokenValid treba da vrati true za validan token i ispravnog korisnika")
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act + Assert
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid treba da vrati false kada token pripada drugom korisniku")
    void isTokenValid_shouldReturnFalse_whenTokenBelongsToDifferentUser() {
        // Arrange — generišemo token za testUser
        String token = jwtService.generateToken(testUser);

        // Drugi korisnik sa RAZLIČITIM emailom
        User anotherUser = User.builder()
                .email("ana@test.com") // drugi email!
                .password("$2a$12$anotherHashedPassword")
                .role(Role.CUSTOMER)
                .build();

        // Act + Assert — token je za marko@test.com, ali proveravamo sa ana@test.com
        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }
}
