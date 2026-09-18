package md.impact.ecommerce.services;

import java.math.BigDecimal;
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
        return strategies.stream()
                .filter(strategy -> strategy.appliesTo(product.getCategory()))
                .findFirst()
                .map(strategy -> strategy.apply(product.getPrice()))
                .orElse(product.getPrice());
    }
}
