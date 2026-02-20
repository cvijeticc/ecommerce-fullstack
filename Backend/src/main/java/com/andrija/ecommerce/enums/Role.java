package com.andrija.ecommerce.enums;

/**
 * Enum koji definiše tipove korisnika u sistemu.
 *
 * ADMIN    — može da upravlja proizvodima, kategorijama i svim porudžbinama
 * CUSTOMER — može da kupuje, vidi svoju korpu i svoje porudžbine
 *
 * Čuvamo ga kao String u bazi (EnumType.STRING) — lakše za čitanje nego broj.
 */
public enum Role {
    ADMIN,
    CUSTOMER
}
