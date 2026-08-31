package md.impact.ecommerce.repositories;

import md.impact.ecommerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByIdAsc();

    List<Product> findByCategoryId(Long categoryId);
}
