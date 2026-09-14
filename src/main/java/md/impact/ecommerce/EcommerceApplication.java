package md.impact.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Punctul de pornire al backend-ului de e-commerce.
 * @EnableCaching activează @Cacheable / @CacheEvict (Lecția 3 — cache Redis).
 */
@SpringBootApplication
@EnableCaching
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
