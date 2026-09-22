package md.impact.ecommerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import md.impact.ecommerce.entities.Role;
import md.impact.ecommerce.entities.User;
import md.impact.ecommerce.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String loginAndGetToken(String email, String password) throws Exception {
        String res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
           .andExpect(status().isOk())
           .andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("token").asText();
    }

    private String adminToken() throws Exception {
        // Register() only ever creates USER accounts — promoting straight to ADMIN
        // via the repository mirrors a real admin account seeded outside the API.
        String email = "admin-" + System.nanoTime() + "@impact.md";
        userRepository.save(new User(email, passwordEncoder.encode("adminpass"), Role.ADMIN));
        return loginAndGetToken(email, "adminpass");
    }


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

    @Test
    void categories_are_public() throws Exception {
        mvc.perform(get("/api/categories"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(2)))
           .andExpect(jsonPath("$[0].name").value("Test Cat A"));
    }

    @Test
    void products_can_be_filtered_by_category() throws Exception {
        mvc.perform(get("/api/products").param("category", "2"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(1)))
           .andExpect(jsonPath("$[0].name").value("Test Product 2"))
           .andExpect(jsonPath("$[0].category").value("Test Cat B"));
    }

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

    @Test
    void product_list_is_cached_until_explicitly_cleared() throws Exception {
        Cache products = cacheManager.getCache("products");
        products.clear();

        mvc.perform(get("/api/products")).andExpect(status().isOk());
        assertThat(products.get("all")).isNotNull();

        mvc.perform(post("/api/cache/clear")).andExpect(status().isOk());
        assertThat(products.get("all")).isNull();
    }

    @Test
    void category_list_is_cached() throws Exception {
        Cache categories = cacheManager.getCache("categories");
        categories.clear();

        mvc.perform(get("/api/categories")).andExpect(status().isOk());
        assertThat(categories.get(SimpleKey.EMPTY)).isNotNull();
    }

    @Test
    void openapi_docs_are_public_and_list_the_routes() throws Exception {
        mvc.perform(get("/v3/api-docs"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("\"/api/products\"")));
    }

    @Test
    void swagger_ui_is_public() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
           .andExpect(status().is3xxRedirection());
    }

    @Test
    void product_in_discounted_category_gets_reduced_final_price() throws Exception {
        mvc.perform(get("/api/products/2"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.price").value(20.50))
           .andExpect(jsonPath("$.finalPrice").value(18.45));
    }

    @Test
    void product_outside_discounted_category_keeps_same_final_price() throws Exception {
        mvc.perform(get("/api/products/1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.price").value(10.00))
           .andExpect(jsonPath("$.finalPrice").value(10.00));
    }

    @Test
    void config_info_is_public_and_reports_app_name() throws Exception {
        mvc.perform(get("/api/config/info"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.appName").value("ecommerce-backend"));
    }

    // --- Lecția 11 — admin-only product management ---------------------------

    @Test
    void creating_a_product_without_a_token_is_401() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\",\"price\":1,\"stock\":1,\"categoryId\":1}"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void creating_a_product_as_a_plain_user_is_403() throws Exception {
        String email = "user-" + System.nanoTime() + "@impact.md";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
           .andExpect(status().isOk());
        String token = loginAndGetToken(email, "secret123");

        mvc.perform(post("/api/products").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\",\"price\":1,\"stock\":1,\"categoryId\":1}"))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void admin_can_create_update_and_delete_a_product() throws Exception {
        String token = adminToken();

        String createRes = mvc.perform(post("/api/products").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Produs nou\",\"description\":\"desc\",\"price\":30.00,\"stock\":5,\"categoryId\":1}"))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.name").value("Produs nou"))
           .andReturn().getResponse().getContentAsString();
        long id = json.readTree(createRes).get("id").asLong();

        mvc.perform(put("/api/products/" + id).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Produs modificat\",\"description\":\"desc\",\"price\":40.00,\"stock\":9,\"categoryId\":1}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name").value("Produs modificat"))
           .andExpect(jsonPath("$.stock").value(9));

        mvc.perform(delete("/api/products/" + id).header("Authorization", "Bearer " + token))
           .andExpect(status().isNoContent());

        mvc.perform(get("/api/products/" + id))
           .andExpect(status().isNotFound());
    }

    @Test
    void admin_can_set_a_manual_discount_that_reduces_final_price() throws Exception {
        String token = adminToken();

        String createRes = mvc.perform(post("/api/products").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Reducere test\",\"price\":100.00,\"stock\":1,\"categoryId\":1,\"discountPercentage\":25}"))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.price").value(100.00))
           .andExpect(jsonPath("$.finalPrice").value(75.00))
           .andReturn().getResponse().getContentAsString();
        long id = json.readTree(createRes).get("id").asLong();

        // Retragerea reducerii (discountPercentage omis din request) readuce
        // prețul final la prețul de listă.
        mvc.perform(put("/api/products/" + id).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Reducere test\",\"price\":100.00,\"stock\":1,\"categoryId\":1}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.finalPrice").value(100.00));
    }

    // --- Lecția 11 — refresh & logout ------------------------------------------

    @Test
    void refresh_token_rotates_and_the_old_one_cannot_be_reused() throws Exception {
        String email = "refresh-" + System.nanoTime() + "@impact.md";
        String body = "{\"email\":\"" + email + "\",\"password\":\"secret123\"}";
        String registerRes = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isOk())
           .andReturn().getResponse().getContentAsString();
        String oldRefreshToken = json.readTree(registerRes).get("refreshToken").asText();
        assertThat(oldRefreshToken).isNotBlank();

        String refreshRes = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + oldRefreshToken + "\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.token").isNotEmpty())
           .andReturn().getResponse().getContentAsString();
        String newRefreshToken = json.readTree(refreshRes).get("refreshToken").asText();
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);

        // Rotation means the old refresh token is now dead — a second use must fail.
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + oldRefreshToken + "\"}"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_with_an_unknown_token_is_401() throws Exception {
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"nu-exista-niciodata\"}"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void logout_revokes_the_refresh_token_so_a_later_refresh_fails() throws Exception {
        String email = "logout-" + System.nanoTime() + "@impact.md";
        String body = "{\"email\":\"" + email + "\",\"password\":\"secret123\"}";
        String registerRes = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isOk())
           .andReturn().getResponse().getContentAsString();
        String refreshToken = json.readTree(registerRes).get("refreshToken").asText();

        mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
           .andExpect(status().isOk());

        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
           .andExpect(status().isUnauthorized());
    }
}
