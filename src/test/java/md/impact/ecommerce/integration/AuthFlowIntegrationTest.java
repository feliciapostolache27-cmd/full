package md.impact.ecommerce.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import md.impact.ecommerce.entities.User;
import md.impact.ecommerce.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("testcontainers-init.sql");

    @DynamicPropertySource
    static void appProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cache.type", () -> "simple");
        registry.add("app.jwt.secret", () -> "testcontainers-secret-key-only-for-lesson-9-0123456789");
        registry.add("app.jwt.expiration-minutes", () -> "60");
        registry.add("app.cors.allowed-origins", () -> "*");
        registry.add("app.discount.category-name", () -> "Accesorii");
        registry.add("app.discount.percentage", () -> "15");
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    UserRepository userRepository;

    @Test
    void register_then_login_then_me_persists_and_reads_from_real_postgres() throws Exception {
        String body = "{\"email\":\"testcontainers@impact.md\",\"password\":\"secret123\"}";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.token").isNotEmpty())
           .andExpect(jsonPath("$.role").value("USER"));

        // Nu doar răspunsul HTTP — verificăm direct în Postgres că a ajuns o
        // singură linie, cu parola hash-uită, nu în clar.
        User saved = userRepository.findByEmail("testcontainers@impact.md").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(saved.getPasswordHash()).startsWith("$2");

        String loginResponse = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode token = json.readTree(loginResponse).get("token");

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token.asText()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.email").value("testcontainers@impact.md"))
           .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void duplicate_email_is_rejected_by_the_real_unique_constraint() throws Exception {
        String body = "{\"email\":\"duplicat@impact.md\",\"password\":\"secret123\"}";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isOk());

        // A doua înregistrare cu același email trebuie respinsă de serviciu
        // *înainte* să ajungă la constrângerea UNIQUE din Postgres — dar dacă
        // vreodată codul ar sări peste verificare, testul tot ar prinde eroarea,
        // pentru că baza de date reală chiar are acea constrângere (H2 o simula).
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isConflict());

        assertThat(userRepository.findByEmail("duplicat@impact.md")).isPresent();
    }
}
