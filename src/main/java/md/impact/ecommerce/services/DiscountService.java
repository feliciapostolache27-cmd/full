package md.impact.ecommerce.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import md.impact.ecommerce.entities.Product;
import md.impact.ecommerce.pricing.DiscountStrategy;

@Service
public class DiscountService {

    private final List<DiscountStrategy> strategies;

    public DiscountService(List<DiscountStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal finalPriceFor(Product product) {
        // Lecția 11 — o reducere manuală, setată de admin pe PRODUS, are
        // prioritate față de Strategy pattern-ul pe categorie din Lecția 5.
        // Rămâne separată de List<DiscountStrategy>: strategiile lucrează pe
        // Category (DiscountStrategy.appliesTo(Category)), nu pe Product — a
        // le forța să cunoască Product doar pentru acest caz ar complica un
        // contract altfel curat, pentru un singur beneficiar.
        BigDecimal manual = product.getDiscountPercentage();
        if (manual != null && manual.signum() > 0) {
            BigDecimal factor = BigDecimal.ONE.subtract(manual.divide(BigDecimal.valueOf(100)));
            return product.getPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }
        return strategies.stream()
                .filter(strategy -> strategy.appliesTo(product.getCategory()))
                .findFirst()
                .map(strategy -> strategy.apply(product.getPrice()))
                .orElse(product.getPrice());
    }
}
