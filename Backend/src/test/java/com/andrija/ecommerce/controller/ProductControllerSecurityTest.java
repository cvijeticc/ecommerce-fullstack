package com.andrija.ecommerce.controller;

import com.andrija.ecommerce.config.CorsConfig;
import com.andrija.ecommerce.config.SecurityConfig;
import com.andrija.ecommerce.security.CustomUserDetailsService;
import com.andrija.ecommerce.security.JwtAuthenticationFilter;
import com.andrija.ecommerce.security.JwtService;
import com.andrija.ecommerce.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security testovi za ProductController.
 *
 * ZAŠTO OVAJ TEST POSTOJI:
 * Ranije je autorizacija bila samo u SecurityConfig-u, gde pravilo
 * .requestMatchers("/api/admin/**").hasRole("ADMIN") NE pokriva /api/products.
 * Write operacije su padale na .anyRequest().authenticated(), pa je SVAKI ulogovan
 * kupac mogao da briše i menja proizvode. Ovi testovi to trajno sprečavaju —
 * ako neko ukloni @PreAuthorize, build pukne.
 *
 * KAKO RADI:
 * @WebMvcTest diže samo web sloj (kontroler + security filteri + @RestControllerAdvice),
 * bez baze i bez pravih servisa — zato je brz (nema Postgres-a).
 * @Import ubacuje pravu security konfiguraciju, jer je baš nju testiramo.
 * @MockitoBean zamenjuje zavisnosti lažnim objektima.
 *
 * @WithMockUser(roles = "CUSTOMER") postavlja lažnog korisnika u SecurityContext,
 * pa nam ne treba pravi JWT token. Spring dodaje prefiks "ROLE_" automatski,
 * tako da roles = "CUSTOMER" daje authority "ROLE_CUSTOMER".
 *
 * JwtAuthenticationFilter je ovde pravi (ne mock) jer mock filtera ne bi pozvao
 * filterChain.doFilter() i lanac bi stao. Pošto testovi ne šalju "Authorization"
 * header, filter odmah propušta zahtev dalje i ne dira SecurityContext.
 *
 * csrf() post-processor: CSRF je ugašen u SecurityConfig-u pa nije neophodan,
 * ali ga navodimo da test ostane ispravan i ako se CSRF jednom uključi.
 */
@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class})
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ─── DELETE /api/products/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/products/{id} — CUSTOMER dobija 403 Forbidden")
    @WithMockUser(roles = "CUSTOMER")
    void deleteProduct_shouldReturn403_whenUserIsCustomer() throws Exception {
        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isForbidden());

        // Ključna provera: servis NIJE ni pozvan — @PreAuthorize je odbio zahtev
        // pre ulaska u metodu kontrolera, dakle proizvod nije obrisan.
        verify(productService, never()).deleteProduct(1L);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} — ADMIN uspešno briše, 204 No Content")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_shouldReturn204_whenUserIsAdmin() throws Exception {
        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} — neulogovan korisnik ne prolazi")
    @WithAnonymousUser
    void deleteProduct_shouldBeDenied_whenAnonymous() throws Exception {
        // Napomena: vraća se 403, a ne 401, jer nemamo custom AuthenticationEntryPoint —
        // Spring Security tada koristi podrazumevani Http403ForbiddenEntryPoint.
        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isForbidden());

        verify(productService, never()).deleteProduct(1L);
    }

    // ─── POST /api/products ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/products — CUSTOMER ne može da kreira proizvod")
    @WithMockUser(roles = "CUSTOMER")
    void createProduct_shouldReturn403_whenUserIsCustomer() throws Exception {
        String json = """
                {
                  "name": "Haker proizvod",
                  "description": "Ne bi smeo da postoji",
                  "price": 1.00,
                  "stockQuantity": 1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isForbidden());
    }

    // ─── GET je javan ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/products/{id} — javan je, prolazi i bez logovanja")
    @WithAnonymousUser
    void getProductById_shouldBePublic() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk());

        verify(productService).getProductById(1L);
    }
}
