package liaison.groble.domain.gig.repository;

import java.util.Optional;

import liaison.groble.domain.gig.entity.Category;

public interface CategoryRepository {
  Optional<Category> findById(Long categoryId);
}
