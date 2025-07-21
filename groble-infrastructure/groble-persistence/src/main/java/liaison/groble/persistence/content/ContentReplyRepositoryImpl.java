package liaison.groble.persistence.content;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.content.entity.ContentReply;
import liaison.groble.domain.content.repository.ContentReplyRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReplyRepositoryImpl implements ContentReplyRepository {
  private final JpaContentReplyRepository jpaContentReplyRepository;

  public ContentReply save(ContentReply contentReply) {
    return jpaContentReplyRepository.save(contentReply);
  }
}
