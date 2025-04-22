package liaison.groble.domain.product.repository;

import java.util.Optional;

import liaison.groble.domain.product.entity.Product;

public interface ProductRepository {
  Optional<Product> findById(Long productId);
}
