package liaison.groble.persistence.content;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.content.dto.FlatReviewReplyDTO;
import liaison.groble.domain.content.entity.QContentReply;
import liaison.groble.domain.content.repository.ContentReplyCustomRepository;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReplyCustomRepositoryImpl implements ContentReplyCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

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

  @Override
  public List<FlatReviewReplyDTO> findRepliesByReviewId(Long reviewId) {
    QContentReply qContentReply = QContentReply.contentReply;
    QUser user = QUser.user;
    return jpaQueryFactory
        .select(
            Projections.fields(
                FlatReviewReplyDTO.class,
                qContentReply.id.as("replyId"),
                qContentReply.createdAt.as("createdAt"),
                qContentReply.seller.userProfile.nickname.as("replierNickname"),
                qContentReply.replyContent.as("replyContent")))
        .from(qContentReply)
        .leftJoin(qContentReply.seller, user)
        .where(qContentReply.contentReview.id.eq(reviewId).and(qContentReply.isDeleted.isFalse()))
        .fetch();
  }
}
