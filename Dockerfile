# Stage 1 ("build") are Maven + JDK-ul complet și compilează jar-ul. Stage 2 pornește
# dintr-o imagine mult mai mică, doar cu un JRE (fără compilator, fără Maven, fără
# codul sursă), și copiază DOAR jar-ul rezultat din stage 1. Rezultatul: imaginea
# finală e mică și nu conține nimic din ce a fost nevoie doar ca să construim jar-ul.

# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiem doar pom.xml întâi ca layer separat — dacă dependențele nu se schimbă,
# Docker refolosește acest layer din cache și sare peste re-descărcarea lor,
# chiar dacă am schimbat codul sursă (COPY src de mai jos invalidează doar layerele
# de după el).
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# ---- Stage 2: run ----
# "jammy" (Ubuntu), nu "alpine": eclipse-temurin nu publică un build alpine pentru
# JRE 17 pe arhitectura arm64 (Apple Silicon) — doar amd64. "jammy" e puțin mai mare,
# dar e multi-arch (amd64 + arm64), deci pornește la fel pe orice laptop din clasă.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/ecommerce-backend-*.jar app.jar

EXPOSE 8080

# Profilul activ vine din SPRING_PROFILES_ACTIVE la rulare (docker run -e ... /
# docker-compose.yml), nu e hardcodat aici — vezi Lecția 6 (12-Factor App).
ENTRYPOINT ["java", "-jar", "app.jar"]
