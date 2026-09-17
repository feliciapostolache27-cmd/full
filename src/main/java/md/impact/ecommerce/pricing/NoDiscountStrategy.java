package md.impact.ecommerce.pricing;

import md.impact.ecommerce.entities.Category;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategia implicită: niciun discount. {@code @Order(LOWEST_PRECEDENCE)} o pune
 * ultima în {@code List<DiscountStrategy>} injectată de Spring — servește drept
 * fallback, ales doar dacă nicio altă strategie nu se potrivește categoriei.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean appliesTo(Category category) {
        return true;
    }

    @Override
    public BigDecimal apply(BigDecimal basePrice) {
        return basePrice;
    }
}
