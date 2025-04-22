package liaison.groble.persistence.product;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.product.entity.Product;
import liaison.groble.domain.product.repository.ProductRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
  private final JpaProductRepository jpaProductRepository;

  @Override
  public Optional<Product> findById(Long productId) {
    return jpaProductRepository.findById(productId);
  }
}
