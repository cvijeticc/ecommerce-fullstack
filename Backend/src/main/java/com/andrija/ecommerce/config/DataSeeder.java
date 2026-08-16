package com.andrija.ecommerce.config;

import com.andrija.ecommerce.entity.Category;
import com.andrija.ecommerce.entity.Product;
import com.andrija.ecommerce.repository.CategoryRepository;
import com.andrija.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Puni bazu demo podacima pri pokretanju aplikacije.
 *
 * Zašto Java klasa a ne data.sql?
 * Uz spring.jpa.hibernate.ddl-auto=update, Hibernate pravi tabele tek posle
 * inicijalizacije DataSource-a, pa bi data.sql pucao na nepostojeće tabele
 * (osim ako se ne uključi defer-datasource-initialization). Osim toga, data.sql
 * nema uslovnu logiku — pri svakom pokretanju bi pokušao isti INSERT i duplirao
 * podatke. CommandLineRunner rešava oboje i radi kroz iste repozitorijume kao
 * ostatak aplikacije.
 *
 * Dve zaštite od toga da ovo završi u produkciji:
 *  1. @Profile("!prod")            — ne registruje se kad je aktivan prod profil
 *  2. @ConditionalOnProperty       — gasi se sa app.seed.enabled=false
 *
 * Idempotentnost: pre svakog upisa proveravamo da li zapis sa tim imenom već
 * postoji. Zato je bezbedno restartovati aplikaciju koliko god puta — podaci se
 * ne dupliraju, a ručno dodati proizvodi se ne diraju.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("DataSeeder: proveravam demo podatke...");

        Category elektronika = category("Elektronika", "Telefoni, laptopovi i gadgeti");
        Category odeca       = category("Odeća i obuća", "Garderoba za sve sezone");
        Category kuca        = category("Kuća i bašta", "Sve za domaćinstvo");
        Category knjige      = category("Knjige", "Domaći i strani naslovi");
        Category sport       = category("Sport i rekreacija", "Oprema za trening i aktivnosti");

        int created = 0;
        created += seed(elektronika, List.of(
                p("Samsung Galaxy S24", "Flagship telefon sa 6.2\" AMOLED ekranom, 256GB memorije i tri kamere.", 89990, 15),
                p("Lenovo IdeaPad 3", "Laptop 15.6\", Ryzen 5, 16GB RAM, 512GB SSD. Za posao i fakultet.", 64990, 8),
                p("Sony WH-1000XM5", "Bežične slušalice sa aktivnim poništavanjem buke, 30h baterije.", 44990, 12),
                p("Xiaomi Redmi Note 13", "Odnos cene i kvaliteta — 120Hz ekran, 5000mAh baterija.", 27990, 25),
                p("Logitech MX Master 3S", "Ergonomski miš za produktivnost, tihi klik, do 3 uređaja.", 12490, 30)
        ));
        created += seed(odeca, List.of(
                p("Nike Air Force 1", "Klasične bele patike od prave kože. Veličine 38–46.", 13990, 20),
                p("Levi's 501 farmerke", "Original straight fit, 100% pamuk, plava indigo boja.", 9990, 18),
                p("Adidas Essentials duks", "Duks sa kapuljačom, unisex model, pamuk i poliester.", 6490, 22),
                p("Columbia zimska jakna", "Vodootporna jakna sa Omni-Heat postavom, do -20°C.", 24990, 6),
                p("Pamučna majica basic", "Osnovna majica kratkih rukava, 180g pamuk, više boja.", 1990, 50)
        ));
        created += seed(kuca, List.of(
                p("Tefal tiganj 28cm", "Neprijanjajući premaz sa Thermo-Spot indikatorom temperature.", 4990, 14),
                p("Philips aparat za kafu", "Espresso aparat sa mlinom za zrna i uređajem za penu.", 18990, 9),
                p("Set peškira 4 kom", "Pamučni frotir 500g/m², dva velika i dva mala peškira.", 3490, 0),
                p("Bosch usisivač", "Usisivač bez kese, 850W, HEPA filter, tih rad.", 27490, 7),
                p("LED stona lampa", "Podesiva temperatura svetla, USB punjenje, savitljivo kućište.", 2790, 33)
        ));
        created += seed(knjige, List.of(
                p("Na Drini ćuprija", "Ivo Andrić — roman za koji je dobio Nobelovu nagradu.", 1290, 40),
                p("Clean Code", "Robert C. Martin — kako se piše kod koji drugi ljudi mogu da čitaju.", 4590, 11),
                p("Sapiens", "Yuval Noah Harari — kratka istorija čovečanstva.", 2190, 19),
                p("Rečnik tehnologije", "Zbornik iz 1981. o odnosu tehnologije i društva.", 1890, 15)
        ));
        created += seed(sport, List.of(
                p("Bučice set 2x5kg", "Par podesivih bučica sa gumenim oblogama, za kućni trening.", 5990, 13),
                p("Prostirka za jogu", "NBR prostirka 10mm, neklizajuća, sa trakom za nošenje.", 2490, 27),
                p("Fudbalska lopta Adidas", "Veličina 5, mašinski šivena, za travu i beton.", 4290, 16),
                p("Bicikl MTB 26\"", "Brdski bicikl, 21 brzina, aluminijumski ram, disk kočnice.", 54990, 4),
                p("Boca za vodu 750ml", "Duplo izolovan inoks, drži hladno 24h i toplo 12h.", 1490, 45)
        ));

        if (created == 0) {
            log.info("DataSeeder: demo podaci već postoje, preskačem.");
        } else {
            log.info("DataSeeder: dodato {} novih proizvoda.", created);
        }
    }

    /**
     * Vraća postojeću kategoriju ili je kreira ako je nema.
     * Ime kategorije je unique u bazi, pa je ono prirodan ključ za ovu proveru.
     */
    private Category category(String name, String description) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    log.info("DataSeeder: kreiram kategoriju '{}'", name);
                    return categoryRepository.save(
                            Category.builder().name(name).description(description).build()
                    );
                });
    }

    /**
     * Upisuje samo one proizvode kojih još nema u bazi.
     * @return koliko je proizvoda stvarno kreirano
     */
    private int seed(Category category, List<Product> products) {
        int created = 0;
        for (Product product : products) {
            if (productRepository.existsByName(product.getName())) {
                continue;
            }
            product.setCategory(category);
            productRepository.save(product);
            created++;
        }
        return created;
    }

    /**
     * Skraćenica za pravljenje proizvoda.
     * Cena ide kroz BigDecimal.valueOf(long) — nikad new BigDecimal(double),
     * jer bi double već uneo grešku zaokruživanja pre nego što stigne do baze.
     */
    private Product p(String name, String description, long priceRsd, int stock) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(BigDecimal.valueOf(priceRsd).setScale(2))
                .stockQuantity(stock)
                .imageUrl("https://picsum.photos/seed/" + slug(name) + "/400/300")
                .build();
    }

    /**
     * Pretvara ime proizvoda u stabilan seed za picsum.photos.
     * Isti proizvod uvek dobija istu sliku — bez ovoga bi se slika menjala
     * pri svakom učitavanju stranice.
     */
    private String slug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
