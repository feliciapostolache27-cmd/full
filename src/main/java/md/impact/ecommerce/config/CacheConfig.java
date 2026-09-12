package md.impact.ecommerce.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import md.impact.ecommerce.dtos.CategoryResponse;
import md.impact.ecommerce.dtos.ProductResponse;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;

/**
 * Configurarea cache-ului Redis (Lecția 3).
 *
 * TTL implicit = 60 de secunde: după un minut, cheia expiră singură și
 * următoarea cerere reîncarcă din baza de date.
 * (Activ doar când spring.cache.type=redis — în teste folosim un cache simplu.)
 *
 * Fiecare cache primește un serializator Jackson legat de tipul lui exact
 * (List<ProductResponse>, List<CategoryResponse>) în loc de unul generic:
 * un serializator "pentru orice tip" trebuie să scrie informația de tip în
 * JSON ca să știe ce reconstruiește, iar pentru un List simplu (care e deja
 * un array JSON) acea informație de tip devine ambiguă la citire. Cunoscând
 * tipul dinainte, nu mai avem nevoie deloc de informația de tip.
 */
@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .disableCachingNullValues();

        return builder -> builder
                .withCacheConfiguration("products", withValueType(base, ProductResponse.class))
                .withCacheConfiguration("categories", withValueType(base, CategoryResponse.class));
    }

    private static <T> RedisCacheConfiguration withValueType(RedisCacheConfiguration base, Class<T> elementType) {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, elementType);
        var serializer = new Jackson2JsonRedisSerializer<List<T>>(new ObjectMapper(), listType);
        return base.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
