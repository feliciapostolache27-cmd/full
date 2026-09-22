package md.impact.ecommerce;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityHeadersTest {

    @Autowired
    MockMvc mvc;

    @Test
    void api_responses_carry_clickjacking_and_sniffing_protection() throws Exception {
        mvc.perform(get("/api/products"))
           .andExpect(status().isOk())
           .andExpect(header().string("X-Frame-Options", "DENY"))
           .andExpect(header().string("X-Content-Type-Options", "nosniff"))
           .andExpect(header().exists("Content-Security-Policy"))
           .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    void swagger_ui_deliberately_skips_frame_protection_because_it_is_embedded() throws Exception {
        // ApiDocsButton din frontend chiar embedează Swagger UI într-un <iframe>
        // (Modal-ul „Documentație API"), pe un origin diferit de backend — dacă
        // X-Frame-Options ar fi prezent aici, browserul ar refuza să-l afișeze și
        // funcția s-ar strica. Doar rutele de documentație sunt scutite — restul
        // API-ului rămâne strict (vezi testul de mai sus).
        mvc.perform(get("/v3/api-docs"))
           .andExpect(status().isOk())
           .andExpect(header().doesNotExist("X-Frame-Options"))
           .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void security_headers_are_exposed_to_cross_origin_javascript() throws Exception {
        // Fără Access-Control-Expose-Headers, un fetch() din frontend (alt origin
        // decât backend-ul) NU poate citi aceste headere din JS, chiar dacă sosesc
        // pe rețea — browserul le ascunde de res.headers.get(...). Consola de
        // securitate din Lecția 10 a frontend-ului depinde direct de asta.
        mvc.perform(get("/api/products").header("Origin", "http://localhost:5500"))
           .andExpect(status().isOk())
           .andExpect(header().string("Access-Control-Expose-Headers", org.hamcrest.Matchers.containsString("X-Frame-Options")));
    }

    @Test
    void sqli_shaped_filter_value_is_rejected_cleanly_not_a_blank_500() throws Exception {
        // "category" e tipizat Long în controller — Spring respinge orice nu se
        // poate converti la Long ÎNAINTE să ajungă la vreo interogare. Payload-ul
        // de mai jos n-ar fi periculos nici dacă ar ajunge la baza de date (JPA
        // leagă parametrii, nu concatenează SQL), dar testul verifică exact
        // punctul din Lecția 10: eroarea trebuie să fie curată, nu un 400 gol.
        mvc.perform(get("/api/products").param("category", "1 OR 1=1"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.message").value("Parametru invalid: category"));
    }

    @Test
    void register_with_an_absurdly_long_password_is_rejected_before_hashing() throws Exception {
        String hugePassword = "a".repeat(500);
        String body = "{\"email\":\"flood@impact.md\",\"password\":\"" + hugePassword + "\"}";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").exists());
    }
}
