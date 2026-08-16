package com.andrija.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — proverava samo da se ceo Spring kontekst podiže bez greške.
 * Hvata pogrešne bean definicije, kružne zavisnosti i loše konfiguracije.
 *
 * properties = "app.seed.enabled=false" gasi DataSeeder tokom testa.
 * Bez toga bi svaki `mvn test` upisivao demo proizvode u razvojnu bazu —
 * test koji menja podatke nije test.
 *
 * Napomena: ovo NIJE isto što i src/test/resources/application.yaml. Taj fajl bi
 * ZASENIO application.yaml iz src/main/resources (isti classpath resurs, test
 * resursi idu prvi), pa bi nestala i konfiguracija baze.
 */
@SpringBootTest(properties = "app.seed.enabled=false")
class EcommerceApplicationTests {

	@Test
	void contextLoads() {
	}

}
