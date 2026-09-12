package md.impact.ecommerce;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import md.impact.ecommerce.dtos.CategoryResponse;
import md.impact.ecommerce.dtos.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifică, fără Redis real, exact ce salvează/citește CacheConfig: o listă de
 * DTO-uri serializată cu Jackson. Un GenericJackson2JsonRedisSerializer simplu
 * eșuează aici (JSON-ul unui List e deja un array, iar informația de tip
 * polimorfic devine ambiguă la citire) — de asta CacheConfig leagă fiecare
 * cache de tipul lui exact în loc de un serializator "pentru orice tip".
 */
class RedisCacheSerializationTest {

    private static <T> Jackson2JsonRedisSerializer<List<T>> listSerializer(Class<T> elementType) {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, elementType);
        return new Jackson2JsonRedisSerializer<>(new ObjectMapper(), listType);
    }

    @Test
    void product_list_round_trips_through_redis_serialization() {
        var serializer = listSerializer(ProductResponse.class);
        List<ProductResponse> value = List.of(
                new ProductResponse(1L, "Tricou", "desc", new BigDecimal("199.00"), 5, "Îmbrăcăminte"));

        byte[] bytes = serializer.serialize(value);
        assertThat(serializer.deserialize(bytes)).isEqualTo(value);
    }

    @Test
    void category_list_round_trips_through_redis_serialization() {
        var serializer = listSerializer(CategoryResponse.class);
        List<CategoryResponse> value = List.of(new CategoryResponse(1L, "Accesorii"));

        byte[] bytes = serializer.serialize(value);
        assertThat(serializer.deserialize(bytes)).isEqualTo(value);
    }
}
