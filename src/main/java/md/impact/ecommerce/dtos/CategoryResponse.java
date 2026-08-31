package md.impact.ecommerce.dtos;

import md.impact.ecommerce.entities.Category;

public record CategoryResponse(Long id, String name) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName());
    }
}
