package liaison.groble.persistence.content;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.content.entity.ContentReply;

public interface JpaContentReplyRepository extends JpaRepository<ContentReply, Long> {}
