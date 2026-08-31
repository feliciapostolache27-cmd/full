package md.impact.ecommerce.dtos;

import md.impact.ecommerce.entities.Product;

import java.math.BigDecimal;

/** Forma unui produs expusă în API (nu trimitem entitatea direct). */
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        String category
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getCategory() == null ? null : p.getCategory().getName()
        );
    }
}
