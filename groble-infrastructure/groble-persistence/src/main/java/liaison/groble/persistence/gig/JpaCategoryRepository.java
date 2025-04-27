package liaison.groble.persistence.gig;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.gig.entity.Category;

public interface JpaCategoryRepository extends JpaRepository<Category, Long> {}
