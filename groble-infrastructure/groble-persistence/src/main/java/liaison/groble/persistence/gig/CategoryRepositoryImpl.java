package liaison.groble.persistence.gig;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.gig.entity.Category;
import liaison.groble.domain.gig.repository.CategoryRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {
  private final JpaCategoryRepository jpaCategoryRepository;

  @Override
  public Optional<Category> findById(Long categoryId) {
    return jpaCategoryRepository.findById(categoryId);
  }
}
