package com.andrija.ecommerce.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracija Swagger / OpenAPI dokumentacije.
 *
 * Bez ove klase springdoc i dalje generiše dokumentaciju i renderuje Swagger UI,
 * ALI nema dugme "Authorize" — pa se zaštićeni endpointovi ne mogu testirati
 * iz browsera jer nema načina da se pošalje "Authorization: Bearer <token>" header.
 *
 * Šta koja anotacija radi:
 *
 * @OpenAPIDefinition — opisuje sam API (naziv, verzija, server).
 *   security = @SecurityRequirement(name = "bearerAuth") postavlja
 *   bearerAuth kao PODRAZUMEVANI zahtev za sve endpointove, pa Swagger
 *   automatski šalje token na svaki poziv kada se jednom autorizuješ.
 *
 * @SecurityScheme — definiše KAKO se šalje autentifikacija:
 *   type = HTTP + scheme = "bearer"  → header "Authorization: Bearer <token>"
 *   bearerFormat = "JWT"             → samo dokumentacija, Swagger ga prikazuje u UI-ju
 *   in = HEADER                      → token ide u header, ne u query ili cookie
 *
 * Kako se koristi:
 * 1. Otvori http://localhost:8080/swagger-ui.html
 * 2. POST /api/auth/login → kopiraj "token" iz odgovora
 * 3. Klikni "Authorize" (gore desno), nalepi SAMO token (bez reči "Bearer")
 * 4. Sada svi zaštićeni endpointovi rade iz Swagger-a
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Ecommerce API",
                version = "1.0",
                description = """
                        REST API za e-commerce aplikaciju — proizvodi, kategorije, korpa i porudžbine.

                        Autentifikacija je JWT (stateless). Registruj se ili uloguj preko /api/auth,
                        pa klikni "Authorize" i nalepi dobijeni token.

                        Role: CUSTOMER (podrazumevano pri registraciji) i ADMIN.
                        """,
                contact = @Contact(name = "Andrija")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Lokalni razvojni server"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Nalepi JWT token dobijen sa POST /api/auth/login (bez prefiksa 'Bearer')",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
