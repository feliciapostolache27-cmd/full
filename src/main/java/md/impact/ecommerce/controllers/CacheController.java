package md.impact.ecommerce.controllers;

import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
