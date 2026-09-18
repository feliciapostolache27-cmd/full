package md.impact.ecommerce.services;

import md.impact.ecommerce.entities.Category;
import md.impact.ecommerce.entities.Product;
import md.impact.ecommerce.pricing.DiscountStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Lecția 8 — testăm mecanismul de selecție al Strategy pattern-ului din
 * Lecția 5 (DiscountService), separat de orice strategie concretă: injectăm
 * DiscountStrategy MOCK-uite direct, ca listă construită de mână — nu cele
 * reale (@Order-uite de Spring). Așa testăm exact regula "prima care se
 * potrivește câștigă", independent de câte strategii reale există în proiect.
 */
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
}
