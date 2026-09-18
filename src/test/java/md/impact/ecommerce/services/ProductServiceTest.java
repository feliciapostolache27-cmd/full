package md.impact.ecommerce.services;

import md.impact.ecommerce.dtos.ProductResponse;
import md.impact.ecommerce.entities.Category;
import md.impact.ecommerce.entities.Product;
import md.impact.ecommerce.exceptions.ApiException;
import md.impact.ecommerce.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Lecția 8 — ProductService are ZERO metode publice care nu ating fie
 * ProductRepository, fie DiscountService — perfect pentru un test unitar: le
 * mock-uim pe amândouă, nu pornim Spring, nu atingem PostgreSQL.
 *
 * Product/Category nu au constructor public (doar Hibernate le construiește,
 * via constructorul protejat) — de asta le mock-uim și pe ELE cu Mockito
 * (@Mock funcționează pe orice clasă non-finală, indiferent de constructor)
 * în loc să încercăm `new Product(...)`.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock DiscountService discountService;

    ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, discountService);
    }

    @Test
    void list_without_a_category_asks_the_repository_for_everything() {
        when(productRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        productService.list(null);

        verify(productRepository).findAllByOrderByIdAsc();
        verify(productRepository, never()).findByCategoryIdOrderByIdAsc(any());
    }

    @Test
    void list_with_a_category_filters_at_the_database_not_in_java() {
        when(productRepository.findByCategoryIdOrderByIdAsc(2L)).thenReturn(List.of());

        productService.list(2L);

        // Important pentru performanță: filtrarea se face într-un query SQL
        // separat (WHERE category_id = ?), nu luând toate produsele și
        // filtrându-le în memorie cu un .stream().filter(...).
        verify(productRepository).findByCategoryIdOrderByIdAsc(2L);
        verify(productRepository, never()).findAllByOrderByIdAsc();
    }

    @Test
    void list_maps_every_product_through_the_discount_service() {
        Product p1 = mock(Product.class);
        Product p2 = mock(Product.class);
        when(p1.getPrice()).thenReturn(new BigDecimal("100.00"));
        when(p2.getPrice()).thenReturn(new BigDecimal("50.00"));
        when(productRepository.findAllByOrderByIdAsc()).thenReturn(List.of(p1, p2));
        when(discountService.finalPriceFor(p1)).thenReturn(new BigDecimal("85.00"));
        when(discountService.finalPriceFor(p2)).thenReturn(new BigDecimal("50.00"));

        List<ProductResponse> result = productService.list(null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).finalPrice()).isEqualByComparingTo("85.00");
        assertThat(result.get(1).finalPrice()).isEqualByComparingTo("50.00");
    }

    @Test
    void get_returns_the_product_with_its_final_price_when_it_exists() {
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        when(product.getId()).thenReturn(7L);
        when(product.getName()).thenReturn("Rucsac pentru laptop");
        when(product.getPrice()).thenReturn(new BigDecimal("899.00"));
        when(product.getCategory()).thenReturn(category);
        when(category.getName()).thenReturn("Accesorii");
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(discountService.finalPriceFor(product)).thenReturn(new BigDecimal("764.15"));

        ProductResponse response = productService.get(7L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.category()).isEqualTo("Accesorii");
        assertThat(response.finalPrice()).isEqualByComparingTo("764.15");
    }

    @Test
    void get_with_a_missing_id_throws_404_not_a_null_pointer() {
        when(productRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.get(9999L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // Dacă produsul nu există, nu are sens să mai întrebăm DiscountService
        // ce preț ar avea un produs care nu există.
        verifyNoInteractions(discountService);
    }
}
