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
`src/main/resources/application.yaml` folosește implicit `postgres` / parola de pe
calculatorul profesorului. **Puneți parola voastră** acolo, sau prin variabile de mediu:
```bash
SPRING_DATASOURCE_PASSWORD=parola_ta ./mvnw spring-boot:run
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
