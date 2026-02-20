package com.andrija.ecommerce.dto;

/**
 * DTO koji se vraća klijentu nakon uspešnog login-a.
 *
 * token — JWT string koji klijent šalje u svakom sledećem zahtevu
 *          u headeru: Authorization: Bearer <token>
 * email — da frontend zna ko je ulogovan
 * role  — da frontend može da prikazuje/sakriva admin delove UI-a
 */
public record LoginResponse(
        String token,
        String email,
        String role
) {}
