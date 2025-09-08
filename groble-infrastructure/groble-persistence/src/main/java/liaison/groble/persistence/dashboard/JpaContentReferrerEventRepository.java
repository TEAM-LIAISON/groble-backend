package liaison.groble.persistence.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.dashboard.entity.ContentReferrerEvent;

public interface JpaContentReferrerEventRepository
    extends JpaRepository<ContentReferrerEvent, Long> {}
