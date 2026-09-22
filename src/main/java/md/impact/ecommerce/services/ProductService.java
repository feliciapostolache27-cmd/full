package md.impact.ecommerce.services;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import md.impact.ecommerce.dtos.ProductRequest;
import md.impact.ecommerce.dtos.ProductResponse;
import md.impact.ecommerce.entities.Category;
import md.impact.ecommerce.entities.Product;
import md.impact.ecommerce.exceptions.ApiException;
import md.impact.ecommerce.repositories.CategoryRepository;
import md.impact.ecommerce.repositories.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DiscountService discountService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                           DiscountService discountService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.discountService = discountService;
    }

    @Cacheable(cacheNames = "products", key = "#categoryId != null ? #categoryId : 'all'")
    public List<ProductResponse> list(Long categoryId) {
        var products = (categoryId == null)
                ? productRepository.findAllByOrderByIdAsc()
                : productRepository.findByCategoryIdOrderByIdAsc(categoryId);
        return products.stream()
                .map(p -> ProductResponse.from(p, discountService.finalPriceFor(p)))
                .toList();
    }

    public ProductResponse get(Long id) {
        return productRepository.findById(id)
                .map(p -> ProductResponse.from(p, discountService.finalPriceFor(p)))
                .orElseThrow(() -> ApiException.notFound("Produsul " + id + " nu există"));
    }

    // Lecția 11 — checkpoint-ul lecției: gestionarea produselor/stocului, doar
    // pentru admin (vezi SecurityConfig). allEntries = true fiindcă cache-ul
    // (Lecția 3) ține câte o intrare per categorie ȘI una pentru "all" — o
    // creare/editare/ștergere poate afecta oricare dintre ele.
    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductResponse create(ProductRequest req) {
        Category category = resolveCategory(req.categoryId());
        Product product = new Product(req.name(), req.description(), req.price(), req.stock(), category);
        product.setDiscountPercentage(req.discountPercentage());
        product = productRepository.save(product);
        return ProductResponse.from(product, discountService.finalPriceFor(product));
    }

    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductResponse update(Long id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Produsul " + id + " nu există"));
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setStock(req.stock());
        product.setCategory(resolveCategory(req.categoryId()));
        product.setDiscountPercentage(req.discountPercentage());
        return ProductResponse.from(product, discountService.finalPriceFor(product));
    }

    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw ApiException.notFound("Produsul " + id + " nu există");
        }
        productRepository.deleteById(id);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ApiException.notFound("Categoria " + categoryId + " nu există"));
    }
}
