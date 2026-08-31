# Impact E-Commerce — Backend (Lecția 1)

Backend de referință Spring Boot pentru practica de la Lecția 1: o singură resursă,
`/api/practice`, care răspunde la toate cele cinci verbe HTTP.

| Verb | Răspuns |
|------|---------|
| `GET`    | `api initialised` |
| `POST`   | `youve posted: {corpul cererii}` |
| `PUT`    | `your update is : {corpul cererii}` |
| `PATCH`  | `you have updated the : {corpul cererii}` |
| `DELETE` | `youve deleted : {corpul cererii}` |

Răspunsurile sunt **text simplu** (`text/plain`). Dacă o cerere nu are corp,
în locul acestuia apare `{}`.

## Cerințe
- Java 17 sau mai nou (`java -version`)
- Maven — sau folosiți wrapper-ul inclus (`./mvnw`), sau porniți din IntelliJ IDEA

## Rulare
```bash
./mvnw spring-boot:run       # Linux / macOS / Git Bash
mvnw.cmd spring-boot:run     # Windows cmd / PowerShell
```
Aplicația pornește pe **http://localhost:8080**.
Din IntelliJ: deschideți `pom.xml` ca proiect și rulați `EcommerceApplication`.

## Test rapid
```bash
curl http://localhost:8080/api/practice
curl -X POST -H "Content-Type: application/json" -d '{"name":"Tricou"}' http://localhost:8080/api/practice
```

## Teste automate
```bash
./mvnw test
```

## Structura pachetelor (layout standard pe straturi)
```
src/main/java/md/impact/ecommerce/
  EcommerceApplication.java   punctul de pornire

  controllers/   controllere REST — primesc cererile HTTP, deleaga, intorc raspunsul
  services/      logica de business — reguli, orchestrarea repository-urilor, Entity <-> DTO
  repositories/  acces la baza de date — interfete Spring Data JPA
  entities/      entitati JPA — clase care corespund tabelelor
  dtos/          Data Transfer Objects — forma datelor schimbate cu clientul
  exceptions/    exceptii proprii + handler global de erori
  config/        configurare (CORS, securitate, beans)

src/main/resources/application.yaml   port + logging
src/test/java/.../controllers/        teste (aceeasi structura ca src/main)
```
La Lecția 1 sunt folosite doar `controllers/` (o rută de practică) și `config/` (CORS).
Restul pachetelor sunt goale acum (cu un `package-info.java` care le descrie) și se
umplu de la Lecția 2 în sus. Fiecare clasă nouă merge în pachetul stratului ei.

## CORS
`config/CorsConfig` acceptă orice origine (`allowedOriginPatterns("*")`), inclusiv
`Origin: null` trimis de un `index.html` deschis prin dublu-click. Este o setare
**doar pentru dezvoltare locală**; în anul 2 o vom restrânge.
