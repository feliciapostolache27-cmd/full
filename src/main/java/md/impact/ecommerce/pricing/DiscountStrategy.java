package md.impact.ecommerce.pricing;

import java.math.BigDecimal;

import md.impact.ecommerce.entities.Category;

public interface DiscountStrategy {

    boolean appliesTo(Category category);

    BigDecimal apply(BigDecimal basePrice);
}
