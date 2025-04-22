package liaison.groble.persistence.product;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.product.entity.Product;

public interface JpaProductRepository extends JpaRepository<Product, Long> {}
