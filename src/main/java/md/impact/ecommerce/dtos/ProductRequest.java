package md.impact.ecommerce.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Folosit atât pentru POST (creare), cât și pentru PUT (înlocuire completă) —
// un admin trimite mereu toate câmpurile, nu doar cele schimbate.
public record ProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal price,
        @NotNull @Min(0) Integer stock,
        Long categoryId,
        // Lecția 11 — opțională: null/lipsă = fără reducere manuală (vezi
        // DiscountService.finalPriceFor).
        @DecimalMin(value = "0", inclusive = true) @DecimalMax(value = "100", inclusive = true) BigDecimal discountPercentage
) {
}
