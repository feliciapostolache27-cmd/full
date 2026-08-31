package md.impact.ecommerce.controllers;

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
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products               -> toate produsele
     * GET /api/products?category=2     -> doar produsele din categoria 2
     */
    @GetMapping
    public List<ProductResponse> list(@RequestParam(name = "category", required = false) Long categoryId) {
        return productService.list(categoryId);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }
}
