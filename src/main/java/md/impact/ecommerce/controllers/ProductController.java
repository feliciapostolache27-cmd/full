package md.impact.ecommerce.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import md.impact.ecommerce.dtos.ProductResponse;
import md.impact.ecommerce.services.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catalogul de produse. Citirile sunt publice; scrierile vor veni în lecțiile următoare. */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Produse", description = "Catalogul de produse (public, cu cache Redis)")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Lista de produse",
            description = "Toate produsele, sau doar cele dintr-o categorie dacă se trimite parametrul `category`.")
    @GetMapping
    public List<ProductResponse> list(@RequestParam(name = "category", required = false) Long categoryId) {
        return productService.list(categoryId);
    }

    @Operation(summary = "Un produs după id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produsul cerut"),
            @ApiResponse(responseCode = "404", description = "Produsul nu există", content = @io.swagger.v3.oas.annotations.media.Content())
    })
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }
}
