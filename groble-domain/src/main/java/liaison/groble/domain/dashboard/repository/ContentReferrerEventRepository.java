package liaison.groble.domain.dashboard.repository;

import liaison.groble.domain.dashboard.entity.ContentReferrerEvent;

public interface ContentReferrerEventRepository {
  ContentReferrerEvent save(ContentReferrerEvent contentReferrerEvent);
}
