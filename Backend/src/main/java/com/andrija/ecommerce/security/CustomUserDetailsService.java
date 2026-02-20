package com.andrija.ecommerce.security;

import com.andrija.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementacija Spring Security UserDetailsService interfejsa.
 *
 * Spring Security poziva ovu klasu tokom autentifikacije da "učita korisnika iz baze".
 * Konkretno: AuthenticationManager.authenticate() interno poziva loadUserByUsername().
 *
 * "Username" u Spring Security terminologiji = naš email.
 * Naš User entitet implementira UserDetails, pa ga direktno vraćamo.
 *
 * @RequiredArgsConstructor — Lombok kreira konstruktor sa svim final poljima.
 * Ovo je preporučeni način injection-a u Spring Boot 3 (umesto @Autowired).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Traži korisnika u bazi po email-u.
     * Poziva se:
     * 1. Pri login-u — AuthenticationManager provjerava lozinku
     * 2. U JwtAuthenticationFilter-u — da učita korisnika za SecurityContext
     *
     * @param email email korisnika (u Spring Security to je "username")
     * @return UserDetails objekat (naš User entitet)
     * @throws UsernameNotFoundException ako korisnik ne postoji u bazi
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Korisnik sa emailom '" + email + "' nije pronađen"
                ));
    }
}
