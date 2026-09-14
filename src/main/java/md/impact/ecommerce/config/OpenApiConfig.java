package md.impact.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentația API cu OpenAPI / Swagger (Lecția 3).
 * Pagina: http://localhost:8080/swagger-ui/index.html
 *
 * Butonul "Authorize" din Swagger UI cere un token JWT (schema "bearerAuth").
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Impact E-Commerce API")
                        .version("v2")
                        .description("Backend de referință — Anul 2. Rutele publice pot fi testate direct; "
                                + "pentru rutele protejate apăsați Authorize și lipiți un token JWT."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
