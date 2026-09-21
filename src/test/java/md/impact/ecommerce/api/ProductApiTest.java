package md.impact.ecommerce.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Lecția 9 — restul testelor din proiect (ApiIntegrationTest) folosesc MockMvc:
 * rapid, dar cererea nu iese niciodată printr-un socket real. Aici pornim
 * aplicația pe un port real (webEnvironment = RANDOM_PORT) și îi vorbim cu
 * RestAssured, exact ca Postman sau un client HTTP adevărat — asta e diferența
 * dintre "am testat controllerul" și "am testat API-ul".
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductApiTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void get_products_returns_the_seeded_catalog() {
        given()
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThan(0))
            .body("[0].name", notNullValue())
            .body("[0].finalPrice", notNullValue());
    }

    @Test
    void get_single_product_in_discounted_category_has_reduced_final_price() {
        given()
        .when()
            .get("/api/products/2")
        .then()
            .statusCode(200)
            .body("price", equalTo(20.50f))
            .body("finalPrice", equalTo(18.45f));
    }

    @Test
    void missing_product_returns_404_with_json_error_body() {
        given()
        .when()
            .get("/api/products/9999")
        .then()
            .statusCode(404)
            .body("message", notNullValue());
    }

    @Test
    void full_register_login_me_flow_over_real_http() {
        String email = "restassured@impact.md";
        String body = "{\"email\":\"" + email + "\",\"password\":\"secret123\"}";

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(200)
            .body("token", notNullValue());

        String token =
            given()
                .contentType(ContentType.JSON)
                .body(body)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .extract().path("token");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/auth/me")
        .then()
            .statusCode(200)
            .body("email", equalTo(email));
    }

    @Test
    void me_without_a_token_is_401_over_real_http() {
        given()
        .when()
            .get("/api/auth/me")
        .then()
            .statusCode(401);
    }
}
