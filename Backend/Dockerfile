# ─── Faza 1: Build ────────────────────────────────────────────────────────────
# Koristimo Maven image sa Java 17 da kompajliramo i pakujemo aplikaciju
FROM maven:3.9-eclipse-temurin-17 AS builder

# Postavljamo radni direktorijum unutar kontejnera
WORKDIR /app

# Kopiramo pom.xml prvo (Docker cache optimizacija)
# Ako se pom.xml nije promenio, Maven dependencies se ne preuzimaju ponovo
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Kopiramo ostatak izvornog koda
COPY src ./src

# Kompajliramo i pravimo JAR (preskačemo testove jer ih pokrećemo odvojeno)
RUN mvn package -DskipTests -q

# ─── Faza 2: Runtime ──────────────────────────────────────────────────────────
# Koristimo manji JRE image (samo runtime, bez Maven i JDK alata)
# eclipse-temurin:17-jre je ~200MB manji od full JDK image-a
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Kopiramo samo JAR iz builder faze (multi-stage build — manji finalni image)
COPY --from=builder /app/target/*.jar app.jar

# Port na kome Spring Boot sluša
EXPOSE 8080

# Pokretanje aplikacije
ENTRYPOINT ["java", "-jar", "app.jar"]
