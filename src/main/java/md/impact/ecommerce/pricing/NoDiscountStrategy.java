package md.impact.ecommerce.pricing;

import java.math.BigDecimal;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import md.impact.ecommerce.entities.Category;

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
