package liaison.groble.persistence.content;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.content.entity.QContentReply;
import liaison.groble.domain.content.repository.ContentReplyCustomRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReplyCustomRepositoryImpl implements ContentReplyCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public void addReply(Long userId, Long reviewId, String replyContent) {
    QContentReply qContentReply = QContentReply.contentReply;

    jpaQueryFactory
        .insert(qContentReply)
        .set(qContentReply.contentReview.id, reviewId)
        .set(qContentReply.seller.id, userId)
        .set(qContentReply.replyContent, replyContent)
        .set(qContentReply.isDeleted, false)
        .execute();
  }
}
