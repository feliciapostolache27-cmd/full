package md.impact.ecommerce.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import md.impact.ecommerce.dtos.ProductRequest;
import md.impact.ecommerce.dtos.ProductResponse;
import md.impact.ecommerce.services.ProductService;

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

    // Lecția 11 — checkpoint: gestionarea produselor/stocului, protejată prin
    // rol. Autorizarea reală e în SecurityConfig (.hasRole("ADMIN")), nu aici —
    // controller-ul doar rutează, exact ca lecția de Clean Code (Lecția 4).
    @Operation(summary = "Creează un produs (doar ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @Operation(summary = "Înlocuiește un produs (doar ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @Operation(summary = "Șterge un produs (doar ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
