package md.impact.ecommerce.pricing;

import md.impact.ecommerce.entities.Category;

import java.math.BigDecimal;

/**
 * Lecția 5 — Strategy pattern. Fiecare implementare decide singură dacă se
 * aplică unei categorii ({@link #appliesTo}) și cum calculează prețul final
 * ({@link #apply}). Nimic altceva din aplicație nu cunoaște regulile concrete —
 * vezi {@link md.impact.ecommerce.services.DiscountService}, care alege prima
 * strategie potrivită dintre toate cele injectate de Spring.
 */
public interface DiscountStrategy {

    boolean appliesTo(Category category);

    BigDecimal apply(BigDecimal basePrice);
}
