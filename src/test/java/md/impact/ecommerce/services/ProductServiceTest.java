package md.impact.ecommerce.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import md.impact.ecommerce.dtos.ProductRequest;
import md.impact.ecommerce.dtos.ProductResponse;
import md.impact.ecommerce.entities.Category;
import md.impact.ecommerce.entities.Product;
import md.impact.ecommerce.exceptions.ApiException;
import md.impact.ecommerce.repositories.CategoryRepository;
import md.impact.ecommerce.repositories.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock DiscountService discountService;

    ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, discountService);
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

    // --- Lecția 11 — create/update/delete, doar ADMIN (verificat în SecurityConfig,
    // nu aici — serviciul presupune că doar cererile autorizate ajung până la el) ---

    @Test
    void create_looks_up_the_category_and_saves_a_new_product() {
        var req = new ProductRequest("Mouse wireless", "2.4GHz", new BigDecimal("199.00"), 25, 2L, null);
        Category category = mock(Category.class);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(discountService.finalPriceFor(any(Product.class))).thenReturn(new BigDecimal("199.00"));

        ProductResponse response = productService.create(req);

        assertThat(response.name()).isEqualTo("Mouse wireless");
        assertThat(response.stock()).isEqualTo(25);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_with_an_unknown_category_fails_before_touching_the_repository() {
        var req = new ProductRequest("Ceva", null, BigDecimal.TEN, 1, 999L, null);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_changes_an_existing_product_in_place() {
        Product existing = mock(Product.class);
        Category category = mock(Category.class);
        var req = new ProductRequest("Nume nou", "descriere nouă", new BigDecimal("50.00"), 3, 1L, null);
        when(productRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(discountService.finalPriceFor(existing)).thenReturn(new BigDecimal("50.00"));

        productService.update(7L, req);

        // Verificăm că modificarea chiar a fost aplicată PE entitate (setterii au
        // fost chemați), nu doar că am construit un răspuns cu datele din request.
        verify(existing).setName("Nume nou");
        verify(existing).setStock(3);
        verify(existing).setCategory(category);
        verify(existing).setDiscountPercentage(null);
    }

    @Test
    void create_sets_the_manual_discount_percentage_on_the_product() {
        var req = new ProductRequest("Căști", null, new BigDecimal("300.00"), 10, null, new BigDecimal("20.00"));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(discountService.finalPriceFor(any(Product.class))).thenReturn(new BigDecimal("240.00"));

        productService.create(req);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getDiscountPercentage()).isEqualByComparingTo("20.00");
    }

    @Test
    void update_can_clear_a_previously_set_manual_discount() {
        Product existing = mock(Product.class);
        var req = new ProductRequest("Căști", null, new BigDecimal("300.00"), 10, null, null);
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(discountService.finalPriceFor(existing)).thenReturn(new BigDecimal("300.00"));

        productService.update(5L, req);

        // O reducere manuală se poate și retrage — null retrimis explicit
        // trebuie să șteargă valoarea veche, nu doar s-o ignore.
        verify(existing).setDiscountPercentage(null);
    }

    @Test
    void update_on_a_missing_product_throws_404() {
        when(productRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(9999L, new ProductRequest("X", null, BigDecimal.ONE, 0, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_removes_an_existing_product() {
        when(productRepository.existsById(7L)).thenReturn(true);

        productService.delete(7L);

        verify(productRepository).deleteById(7L);
    }

    @Test
    void delete_on_a_missing_product_throws_404_instead_of_deleting_nothing_silently() {
        when(productRepository.existsById(9999L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(9999L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(productRepository, never()).deleteById(any());
    }
}
