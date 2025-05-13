package liaison.groble.domain.content.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.content.entity.Category;

public interface CategoryRepository {
  Optional<Category> findById(Long categoryId);

  List<Category> findAll();

  void saveAll(List<Category> categories);
}
