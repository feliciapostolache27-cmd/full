package md.impact.ecommerce.dtos;

import java.math.BigDecimal;

import md.impact.ecommerce.entities.Product;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal finalPrice,
        int stock,
        String category,
        // Lecția 11 — reducerea manuală brută (null dacă nu e setată), ca să
        // poată fi precompletată în formularul de editare din admin.
        BigDecimal discountPercentage
) {
    public static ProductResponse from(Product p, BigDecimal finalPrice) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                finalPrice,
                p.getStock(),
                p.getCategory() == null ? null : p.getCategory().getName(),
                p.getDiscountPercentage()
        );
    }
}
