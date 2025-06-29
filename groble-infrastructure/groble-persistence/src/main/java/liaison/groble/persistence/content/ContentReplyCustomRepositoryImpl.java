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

  @Override
  public void updateReply(Long userId, Long reviewId, Long replyId, String replyContent) {
    QContentReply qContentReply = QContentReply.contentReply;
    jpaQueryFactory
        .update(qContentReply)
        .set(qContentReply.replyContent, replyContent)
        .where(
            qContentReply
                .id
                .eq(replyId)
                .and(qContentReply.contentReview.id.eq(reviewId))
                .and(qContentReply.seller.id.eq(userId)))
        .execute();
  }

  @Override
  public void deleteReply(Long userId, Long reviewId, Long replyId) {
    QContentReply qContentReply = QContentReply.contentReply;
    jpaQueryFactory
        .update(qContentReply)
        .set(qContentReply.isDeleted, true)
        .where(
            qContentReply
                .id
                .eq(replyId)
                .and(qContentReply.contentReview.id.eq(reviewId))
                .and(qContentReply.seller.id.eq(userId)))
        .execute();
  }
}
