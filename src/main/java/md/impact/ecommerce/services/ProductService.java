package md.impact.ecommerce.services;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import md.impact.ecommerce.dtos.ProductResponse;
import md.impact.ecommerce.exceptions.ApiException;
import md.impact.ecommerce.repositories.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final DiscountService discountService;

    public ProductService(ProductRepository productRepository, DiscountService discountService) {
        this.productRepository = productRepository;
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
}
