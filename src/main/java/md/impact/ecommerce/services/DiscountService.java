package md.impact.ecommerce.services;

import md.impact.ecommerce.entities.Product;
import md.impact.ecommerce.pricing.DiscountStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lecția 5 — alege strategia de discount potrivită dintre toate cele declarate
 * ca beans Spring. Nu există niciun switch/if-else pe tipul de strategie:
 * Spring injectează {@code List<DiscountStrategy>} deja ordonată după
 * {@code @Order}, iar serviciul ia prima care se potrivește categoriei
 * produsului. Asta e DI-ul Spring implementând Strategy fără cod suplimentar.
 */
@Service
public class DiscountService {

    private final List<DiscountStrategy> strategies;

    public DiscountService(List<DiscountStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal finalPriceFor(Product product) {
        return strategies.stream()
                .filter(strategy -> strategy.appliesTo(product.getCategory()))
                .findFirst()
                .map(strategy -> strategy.apply(product.getPrice()))
                .orElse(product.getPrice());
    }
}
