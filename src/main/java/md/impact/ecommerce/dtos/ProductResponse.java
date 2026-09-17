package md.impact.ecommerce.dtos;

import md.impact.ecommerce.entities.Product;

import java.math.BigDecimal;

/** Forma unui produs expusă în API (nu trimitem entitatea direct). */
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        // Lecția 5: prețul după aplicarea strategiei de discount potrivite
        // (vezi DiscountService); egal cu `price` dacă nicio reducere nu se aplică.
        BigDecimal finalPrice,
        int stock,
        String category
) {
    public static ProductResponse from(Product p, BigDecimal finalPrice) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                finalPrice,
                p.getStock(),
                p.getCategory() == null ? null : p.getCategory().getName()
        );
    }
}
