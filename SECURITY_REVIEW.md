# Revizuire de securitate — Lecția 10

Trecere OWASP Top 10 peste endpoint-urile existente ale proiectului (nu un
exercițiu ipotetic — fiecare rând de mai jos a fost verificat direct în cod,
cu un test care îl demonstrează).

| # | Zonă | Ce am găsit | Remediere |
|---|------|-------------|-----------|
| 1 | Clickjacking (A05) | `SecurityConfig` dezactiva `frameOptions` **global**, pe tot API-ul. Motivul era real — `ApiDocsButton` din frontend embedează Swagger UI într-un `<iframe>` adevărat, pe un origin diferit de backend, deci pagina chiar are nevoie să poată fi afișată în cadru — dar remedierea greșită proteja Swagger UI dezactivând protecția peste tot, inclusiv pe `/api/products`, `/api/auth/**` etc., care n-au niciun motiv să fie embedate. | Două `SecurityFilterChain`, cu `@Order` diferit: unul cu `securityMatcher("/swagger-ui/**", "/v3/api-docs/**")` care păstrează `frameOptions.disable()` doar acolo, altul (restul API-ului) cu `frameOptions.deny()` + `Content-Security-Policy: frame-ancestors 'none'`. Verificat în `SecurityHeadersTest` — un test pentru fiecare lanț. |
| 2 | MIME sniffing (A05) | Niciun header `X-Content-Type-Options` pe răspunsuri. | `contentTypeOptions(withDefaults())` → `nosniff`. Verificat în `SecurityHeadersTest`. |
| 3 | Scurgere de referrer (A05) | Niciun `Referrer-Policy` explicit. | `STRICT_ORIGIN_WHEN_CROSS_ORIGIN`. Verificat în `SecurityHeadersTest`. |
| 4 | Gestionarea erorilor (A05) | Un parametru cu tip greșit (ex. `?category=abc`, forma tipică a unei încercări de SQL injection sau a unui scanner automat) întorcea un 400 cu **corp gol** — inconsecvent cu restul API-ului, care întoarce mereu `{status, message}`. | `GlobalExceptionHandler` prinde acum `MethodArgumentTypeMismatchException`. Verificat în `SecurityHeadersTest`. |
| 5 | SQL Injection (A03) | Verificat, nu remediat — nu era nimic de remediat. Toate query-urile trec prin Spring Data JPA (`ProductRepository`, `CategoryRepository`, `UserRepository`), care leagă parametrii, nu concatenează SQL. Un payload gen `1 OR 1=1` nici măcar nu ajunge la o interogare — e respins la nivel de conversie de tip, înainte de controller. | — (deja sigur; testat explicit ca să rămână așa). |
| 6 | CORS (A05) | `allowedOriginPatterns` e `*` pe `dev`, dar `allowCredentials(false)` — combinația e sigură fiindcă JWT-ul circulă prin header-ul `Authorization`, nu prin cookie-uri; un wildcard cu credențiale ar fi fost periculos, unul fără, nu. | — (deja sigur; documentat aici ca să nu fie „reparat" greșit mai târziu prin activarea credențialelor). |
| 7 | Validarea input-ului (A03/A04) | `RegisterRequest` avea deja `@Email`/`@NotBlank`/`@Size(max = 72)`. Verificat că o parolă absurd de lungă (500 caractere) e respinsă *înainte* să ajungă la BCrypt, nu doar trunchiată silențios. | — (deja sigur; testat explicit). |
| 8 | Parole (A02) | Verificat, nu remediat — BCrypt (Lecția 27, Anul 1) e deja folosit peste tot, niciodată text în clar. | — (deja sigur). |

## Ce NU e în scopul acestei lecții

Rate limiting (Bucket4j) e Lecția 16 — nu adăugăm limitare de request-uri
acum, ca să nu suprapunem conținutul. OAuth2/social login e Lecția 11.
