package liaison.groble.persistence.dashboard;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.dashboard.entity.ContentReferrerEvent;
import liaison.groble.domain.dashboard.repository.ContentReferrerEventRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReferrerEventRepositoryImpl implements ContentReferrerEventRepository {
  private final JpaContentReferrerEventRepository jpaContentReferrerEventRepository;

  @Override
  public ContentReferrerEvent save(ContentReferrerEvent contentReferrerEvent) {
    return jpaContentReferrerEventRepository.save(contentReferrerEvent);
  }
}
