package liaison.groble.persistence.content;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.content.entity.Category;
import liaison.groble.domain.content.repository.CategoryRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {
  private final JpaCategoryRepository jpaCategoryRepository;

  @Override
  public Optional<Category> findById(Long categoryId) {
    return jpaCategoryRepository.findById(categoryId);
  }

  @Override
  public Optional<Category> findByCode(String categoryCode) {
    return jpaCategoryRepository.findByCode(categoryCode);
  }

  @Override
  public List<Category> findAll() {
    return jpaCategoryRepository.findAll();
  }

  @Override
  public void saveAll(List<Category> categories) {
    jpaCategoryRepository.saveAll(categories);
  }
}
