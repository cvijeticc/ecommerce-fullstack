package com.andrija.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servis koji se bavi JWT tokenima — kreiranje, validacija i čitanje podataka.
 *
 * Šta je JWT?
 * JSON Web Token = HEADER.PAYLOAD.SIGNATURE (Base64 enkodovano, odvojeno tačkama).
 * - Header: algoritam (HS256)
 * - Payload: "claims" — email, role, iat (issued at), exp (expiration)
 * - Signature: HMAC-SHA256(header + payload, secretKey) — sprečava falsifikovanje
 *
 * Stateless: server NE čuva token u bazi! Samo verifikuje potpis sa secret ključem.
 */
@Service
public class JwtService {

    // Čitamo iz application.yaml: jwt.secret
    @Value("${jwt.secret}")
    private String secretKey;

    // Čitamo iz application.yaml: jwt.expiration (86400000 ms = 24h)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Kreira JWT token za korisnika.
     * U payload upisujemo email (kao "subject") i role.
     *
     * @param userDetails Spring Security objekat koji predstavlja korisnika
     * @return JWT string koji klijent čuva i šalje u svakom zahtevu
     */
    public String generateToken(UserDetails userDetails) {
        // Extra claims — dodatni podaci koje dodajemo u payload
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(Object::toString)
                .orElse(""));

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // username = email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey()) // HMAC-SHA256 potpis
                .compact(); // kreira finalni "header.payload.signature" string
    }

    /**
     * Izvlači email iz tokena (email je "subject" u JWT payload-u).
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Proverava da li je token validan:
     * 1. Email iz tokena mora odgovarati email-u korisnika
     * 2. Token ne sme biti istekao
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Proverava da li je token istekao (exp claim < trenutno vreme).
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generička metoda za izvlačenje bilo kog claim-a iz tokena.
     * Koristi Function<Claims, T> — npr. Claims::getSubject, Claims::getExpiration.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Dekodira token i vraća sve claim-ove iz payload-a.
     * Ako je potpis nevalidan ili token istekao — baca JwtException.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // verifikuj potpis sa secret ključem
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Pretvara hex string iz application.yaml u javax.crypto.SecretKey objekat.
     * Keys.hmacShaKeyFor() automatski bira pravi SHA algoritam na osnovu dužine ključa.
     */
    private SecretKey getSigningKey() {
        // Konvertujemo hex string u byte niz
        byte[] keyBytes = hexToBytes(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Pomoćna metoda: konvertuje hex string u byte niz.
     * "4F3E..." -> new byte[]{ 0x4F, 0x3E, ... }
     */
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
