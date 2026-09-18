# Impact E-Commerce — Backend

Backend de referință Spring Boot pentru Anul 2. Crește lecție cu lecție.

## Lecția 1 — ruta de practică (`/api/practice`)
| Verb | Răspuns (`text/plain`) |
|------|------------------------|
| `GET`    | `api initialised` |
| `POST`   | `youve posted: {corp}` |
| `PUT`    | `your update is : {corp}` |
| `PATCH`  | `you have updated the : {corp}` |
| `DELETE` | `youve deleted : {corp}` |

## Lecția 2 — PostgreSQL + entități + JWT
| Metodă & rută | Acces | Descriere |
|---|---|---|
| `GET /api/products`             | public | toate produsele din baza de date |
| `GET /api/products?category={id}` | public | doar produsele dintr-o categorie |
| `GET /api/products/{id}`        | public | un produs (404 dacă nu există) |
| `GET /api/categories`           | public | lista de categorii |
| `POST /api/auth/register`       | public | creează un cont, întoarce un JWT |
| `POST /api/auth/login`          | public | autentificare, întoarce un JWT |
| `GET /api/auth/me`              | **token** | utilizatorul curent |

Erorile vin ca JSON: `{ "status": 404, "message": "..." }`.

## Cerințe
- Java 17+
- PostgreSQL local, cu baza de date **`e-commerce`** creată și populată
  (rulați scripturile din `2 Year/Lesson 2/`: `create_database.sql` → `create_tables.sql` → `insert_data.sql`)

## Configurarea bazei de date
De la Lecția 6, config-ul e împărțit pe profiluri (`application-dev.yaml` /
`application-prod.yaml` — vezi Lecția 6 mai jos). Pe `dev` (implicit), doar parola diferă
de obicei; restul are valori implicite:
```bash
APP_DB_PASSWORD=parola_ta ./mvnw spring-boot:run
```
`ddl-auto` este `none` — schema vine din scripturile SQL, nu din ORM.

## Rulare
```bash
./mvnw spring-boot:run       # Git Bash / Linux / macOS
mvnw.cmd spring-boot:run     # Windows cmd / PowerShell
```
Pornește pe **http://localhost:8080**. Din IntelliJ: deschideți `pom.xml`, rulați `EcommerceApplication`.

## Test rapid
```bash
curl http://localhost:8080/api/products
curl -X POST -H "Content-Type: application/json" \
     -d '{"email":"user@impact.md","password":"user123"}' \
     http://localhost:8080/api/auth/login
```
Conturi din `insert_data.sql`: `admin@impact.md` / `admin123` (ADMIN), `user@impact.md` / `user123` (USER).

## Teste automate
```bash
./mvnw test
```
Testele folosesc o bază de date **H2 în memorie** (nu PostgreSQL) — rulează oriunde și
nu ating datele voastre reale.

## Structura pachetelor (layout standard pe straturi)
```
src/main/java/md/impact/ecommerce/
  EcommerceApplication.java

  controllers/   PracticeController, ProductController, CategoryController, AuthController
  services/      ProductService, CategoryService, AuthService
  repositories/  ProductRepository, CategoryRepository, UserRepository
  entities/      Product, Category, User, Role
  dtos/          ProductResponse, RegisterRequest, LoginRequest, AuthResponse
  exceptions/    ApiException + GlobalExceptionHandler
  security/      JwtService, JwtAuthFilter
  config/        CorsConfig, SecurityConfig
```
Fiecare pachet gol are un `package-info.java` care îl descrie. Fiecare clasă nouă
merge în pachetul stratului ei.

## CORS
`config/CorsConfig` + `config/SecurityConfig` acceptă orice origine (inclusiv
`Origin: null` de la un `index.html` deschis local). Setare **doar pentru local**.

## Lecția 6 — profiluri Spring (dev/prod)
`application.yaml` ține doar config-ul comun; `application-dev.yaml` (valori implicite,
pentru laptop) și `application-prod.yaml` (fără nicio valoare implicită — pornirea
eșuează dacă lipsește o variabilă) țin restul. Profilul activ vine din
`SPRING_PROFILES_ACTIVE` (implicit `dev`). Toate variabilele sunt documentate în
`.env.example`. `GET /api/config/info` arată profilul activ.

## Lecția 7 — Docker & Docker Compose
Tot proiectul (backend + PostgreSQL + Redis) rulează cu un singur comandă, fără nimic
instalat pe laptop în afară de Docker:
```bash
docker compose up --build
```
Pornește pe **http://localhost:8080**, la fel ca rularea directă — `docker-compose.yml`
doar setează variabilele de mediu din Lecția 6 (`APP_DB_HOST=postgres`,
`APP_REDIS_HOST=redis`) ca backend-ul să găsească vecinii lui din rețeaua Docker, în loc
de `localhost`. Niciun cod nu știe că rulează în Docker.

- `Dockerfile` — build multi-stage: stage 1 (`maven:3.9-eclipse-temurin-17`) compilează
  jar-ul; stage 2 (`eclipse-temurin:17-jre-alpine`) rulează doar jar-ul rezultat, fără
  Maven/JDK complet/cod sursă în imaginea finală.
- `docker-compose.yml` — trei servicii: `postgres` (cu tabelele + datele de test din
  `docker/init-db/`, rulate automat la primul start), `redis`, `backend` (construit din
  `Dockerfile`-ul de mai sus).
- Volumul `postgres-data` persistă datele între restart-uri; `docker compose down -v`
  șterge totul și repornește de la zero (util dacă vrei să retești seed-ul din
  `docker/init-db/`).

**Erori frecvente:**
- `port is already allocated` — ceva rulează deja pe 5432/6379/8080 pe laptop
  (ex. Postgres/Redis instalate local pentru lecțiile anterioare). Oprește-le, sau
  schimbă porturile din stânga (`"5433:5432"`) în `docker-compose.yml`.
- Backend-ul pornește înaintea bazei de date și crapă la conexiune — de asta există
  `depends_on` + `healthcheck`: Compose așteaptă `pg_isready`/`redis-cli ping` înainte
  de a porni `backend`, nu doar ca containerul Postgres să existe.
- Ai modificat codul, dar `docker compose up` rulează tot varianta veche — imaginea nu
  se reconstruiește automat; ai nevoie de `docker compose up --build`.
