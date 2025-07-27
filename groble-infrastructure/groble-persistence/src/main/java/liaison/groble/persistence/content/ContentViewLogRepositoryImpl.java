package liaison.groble.persistence.content;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.content.entity.ContentViewLog;
import liaison.groble.domain.content.repository.ContentViewLogRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ContentViewLogRepositoryImpl implements ContentViewLogRepository {

  private final JpaContentViewLogRepository jpaContentViewLogRepository;

  @Override
  public ContentViewLog save(ContentViewLog contentViewLog) {
    return jpaContentViewLogRepository.save(contentViewLog);
  }
}
