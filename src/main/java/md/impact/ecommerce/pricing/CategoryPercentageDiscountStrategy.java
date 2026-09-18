package md.impact.ecommerce.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import md.impact.ecommerce.entities.Category;

@Component
@Order(1)
public class CategoryPercentageDiscountStrategy implements DiscountStrategy {

    private final String discountedCategory;
    private final BigDecimal percentage;

    public CategoryPercentageDiscountStrategy(
            @Value("${app.discount.category-name}") String discountedCategory,
            @Value("${app.discount.percentage}") BigDecimal percentage) {
        this.discountedCategory = discountedCategory;
        this.percentage = percentage;
    }

    @Override
    public boolean appliesTo(Category category) {
        return category != null && discountedCategory.equalsIgnoreCase(category.getName());
    }

    @Override
    public BigDecimal apply(BigDecimal basePrice) {
        BigDecimal factor = BigDecimal.ONE.subtract(percentage.divide(BigDecimal.valueOf(100)));
        return basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
