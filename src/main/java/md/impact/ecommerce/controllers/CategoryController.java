package md.impact.ecommerce.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import md.impact.ecommerce.dtos.CategoryResponse;
import md.impact.ecommerce.services.CategoryService;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorii", description = "Categoriile de produse (public, cu cache Redis)")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Lista de categorii")
    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.list();
    }
}
