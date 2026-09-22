package md.impact.ecommerce.services;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import md.impact.ecommerce.entities.Category;
import md.impact.ecommerce.entities.Product;
import md.impact.ecommerce.pricing.DiscountStrategy;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock Product product;
    @Mock Category category;

    @Test
    void picks_the_first_strategy_that_applies_to_the_category() {
        BigDecimal basePrice = new BigDecimal("100.00");
        when(product.getPrice()).thenReturn(basePrice);
        when(product.getCategory()).thenReturn(category);

        DiscountStrategy doesNotApply = mock(DiscountStrategy.class);
        when(doesNotApply.appliesTo(category)).thenReturn(false);

        DiscountStrategy applies = mock(DiscountStrategy.class);
        when(applies.appliesTo(category)).thenReturn(true);
        when(applies.apply(basePrice)).thenReturn(new BigDecimal("85.00"));

        DiscountService discountService = new DiscountService(List.of(doesNotApply, applies));

        BigDecimal result = discountService.finalPriceFor(product);

        assertThat(result).isEqualByComparingTo("85.00");
        verify(doesNotApply, never()).apply(any());
    }

    @Test
    void stops_at_the_first_match_even_if_a_later_strategy_would_also_apply() {
        // Ordinea listei = ordinea @Order din Spring. Dacă DOUĂ strategii s-ar
        // potrivi vreodată aceleiași categorii (nu ar trebui, dar să zicem),
        // doar prima e folosită — a doua nici măcar nu e întrebată.
        BigDecimal basePrice = new BigDecimal("100.00");
        when(product.getPrice()).thenReturn(basePrice);
        when(product.getCategory()).thenReturn(category);

        DiscountStrategy first = mock(DiscountStrategy.class);
        when(first.appliesTo(category)).thenReturn(true);
        when(first.apply(basePrice)).thenReturn(new BigDecimal("90.00"));

        DiscountStrategy second = mock(DiscountStrategy.class);
        lenient().when(second.appliesTo(category)).thenReturn(true);

        DiscountService discountService = new DiscountService(List.of(first, second));

        BigDecimal result = discountService.finalPriceFor(product);

        assertThat(result).isEqualByComparingTo("90.00");
        verifyNoInteractions(second);
    }

    @Test
    void falls_back_to_the_original_price_when_no_strategy_applies() {
        BigDecimal basePrice = new BigDecimal("49.00");
        when(product.getPrice()).thenReturn(basePrice);
        when(product.getCategory()).thenReturn(category);

        DiscountStrategy neverApplies = mock(DiscountStrategy.class);
        when(neverApplies.appliesTo(category)).thenReturn(false);

        DiscountService discountService = new DiscountService(List.of(neverApplies));

        BigDecimal result = discountService.finalPriceFor(product);

        assertThat(result).isEqualByComparingTo(basePrice);
    }

    @Test
    void with_no_strategies_registered_at_all_the_price_is_unchanged() {
        // Un caz limită onest: dacă cineva șterge accidental toate beans-urile
        // DiscountStrategy din context, aplicația tot nu trebuie să crape la
        // afișarea catalogului — doar nu mai aplică nicio reducere.
        when(product.getPrice()).thenReturn(new BigDecimal("10.00"));

        DiscountService discountService = new DiscountService(List.of());

        BigDecimal result = discountService.finalPriceFor(product);

        assertThat(result).isEqualByComparingTo("10.00");
    }

    // --- Lecția 11 — reducere manuală per produs ------------------------------

    @Test
    void a_manual_product_discount_wins_over_any_category_strategy() {
        when(product.getPrice()).thenReturn(new BigDecimal("100.00"));
        when(product.getDiscountPercentage()).thenReturn(new BigDecimal("25"));

        DiscountStrategy categoryStrategy = mock(DiscountStrategy.class);
        lenient().when(categoryStrategy.appliesTo(any())).thenReturn(true);

        DiscountService discountService = new DiscountService(List.of(categoryStrategy));

        BigDecimal result = discountService.finalPriceFor(product);

        assertThat(result).isEqualByComparingTo("75.00");
        // Reducerea manuală răspunde direct — strategia pe categorie nici nu
        // trebuie întrebată dacă produsul are deja una setată.
        verifyNoInteractions(categoryStrategy);
    }

    @Test
    void a_zero_manual_discount_is_treated_as_no_manual_discount() {
        // 0% setat explicit nu trebuie tratat ca "reducere activă" — altfel un
        // admin care retrage o reducere punând 0 în loc de gol ar bloca
        // strategia de categorie fără niciun motiv.
        when(product.getPrice()).thenReturn(new BigDecimal("50.00"));
        when(product.getCategory()).thenReturn(category);
        when(product.getDiscountPercentage()).thenReturn(BigDecimal.ZERO);

        DiscountStrategy categoryStrategy = mock(DiscountStrategy.class);
        when(categoryStrategy.appliesTo(category)).thenReturn(true);
        when(categoryStrategy.apply(new BigDecimal("50.00"))).thenReturn(new BigDecimal("45.00"));

        DiscountService discountService = new DiscountService(List.of(categoryStrategy));

        BigDecimal result = discountService.finalPriceFor(product);

        assertThat(result).isEqualByComparingTo("45.00");
    }
}
