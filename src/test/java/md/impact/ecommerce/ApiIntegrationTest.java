package md.impact.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    // -------- Lecția 1: ruta de practică rămâne publică --------

    @Test
    void practice_get_still_works() throws Exception {
        mvc.perform(get("/api/practice"))
           .andExpect(status().isOk())
           .andExpect(content().string("api initialised"));
    }

    @Test
    void practice_post_echoes_body() throws Exception {
        mvc.perform(post("/api/practice").contentType(MediaType.APPLICATION_JSON).content("{\"a\":1}"))
           .andExpect(status().isOk())
           .andExpect(content().string("youve posted: {\"a\":1}"));
    }

    // -------- Lecția 2: produse din baza de date --------

    @Test
    void products_are_public_and_come_from_db() throws Exception {
        mvc.perform(get("/api/products"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(2)))
           .andExpect(jsonPath("$[0].name").value("Test Product 1"))
           .andExpect(jsonPath("$[0].category").value("Test Cat A"));
    }

    @Test
    void missing_product_returns_404_with_message() throws Exception {
        mvc.perform(get("/api/products/9999"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").exists());
    }

    // -------- Lecția 2: înregistrare + autentificare JWT --------

    @Test
    void register_then_login_returns_token_and_me_works() throws Exception {
        String body = "{\"email\":\"elev@impact.md\",\"password\":\"secret123\"}";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.token").isNotEmpty())
           .andExpect(jsonPath("$.role").value("USER"));

        String res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isOk())
           .andReturn().getResponse().getContentAsString();
        JsonNode token = json.readTree(res).get("token");

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token.asText()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.email").value("elev@impact.md"));
    }

    @Test
    void wrong_password_is_401() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.md\",\"password\":\"correct1\"}"))
           .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.md\",\"password\":\"wrongpass\"}"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void protected_route_without_token_is_401() throws Exception {
        mvc.perform(get("/api/auth/me"))
           .andExpect(status().isUnauthorized());
    }
}
