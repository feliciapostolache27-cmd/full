package md.impact.ecommerce.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Golirea manuală a cache-ului (Lecția 3).
 *
 * Fără această rută, cache-ul expiră singur după TTL (60s). Butonul o folosește
 * ca să vedeți imediat diferența dintre "din cache" și "din baza de date".
 * În producție golirea s-ar face automat, la fiecare modificare de produs
 * (@CacheEvict pe metodele de scriere) — vezi Lecția 13.
 */
@RestController
@RequestMapping("/api/cache")
@Tag(name = "Cache", description = "Utilitar pentru demonstrarea cache-ului Redis")
public class CacheController {

    @Operation(summary = "Golește cache-ul de produse și categorii")
    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "categories", allEntries = true)
    })
    @PostMapping("/clear")
    public Map<String, String> clear() {
        return Map.of("status", "cache golit");
    }
}
