package liaison.groble.persistence.content;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.Category;

public interface JpaCategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByCode(String code);
}
