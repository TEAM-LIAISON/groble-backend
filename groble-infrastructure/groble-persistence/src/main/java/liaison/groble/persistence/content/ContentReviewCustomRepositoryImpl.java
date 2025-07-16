package liaison.groble.persistence.content;

import static com.querydsl.jpa.JPAExpressions.select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.content.dto.FlatContentReviewReplyDTO;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.entity.QContentReply;
import liaison.groble.domain.content.entity.QContentReview;
import liaison.groble.domain.content.enums.ReviewStatus;
import liaison.groble.domain.content.repository.ContentReviewCustomRepository;
import liaison.groble.domain.purchase.entity.QPurchase;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReviewCustomRepositoryImpl implements ContentReviewCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<ContentReview> getContentReview(Long userId, Long reviewId) {
    QContentReview qContentReview = QContentReview.contentReview;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    BooleanExpression conditions =
        qContentReview
            .id
            .eq(reviewId)
            .and(qContent.user.id.eq(userId))
            .and(qContentReview.user.id.eq(qUser.id))
            .and(qContentReview.reviewStatus.eq(ReviewStatus.ACTIVE));

    return Optional.ofNullable(
        jpaQueryFactory
            .selectFrom(qContentReview)
            .leftJoin(qContentReview.content, qContent)
            .leftJoin(qContentReview.user, qUser)
            .where(conditions)
            .fetchOne());
  }

  @Override
  public Page<FlatContentReviewDetailDTO> getContentReviewPageDTOs(
      Long userId, Long contentId, Pageable pageable) {
    QUser qUser = QUser.user;
    QContent qContent = QContent.content;
    QContentReview qContentReview = QContentReview.contentReview;
    QPurchase qPurchase = QPurchase.purchase;

    // 조건: 해당 콘텐츠에 대한 리뷰이며, 해당 콘텐츠의 소유자가 userId인 경우
    BooleanExpression cond =
        qContentReview
            .content
            .id
            .eq(contentId)
            .and(qContent.user.id.eq(userId))
            .and(qContentReview.reviewStatus.eq(ReviewStatus.ACTIVE));

    // QueryDSL PathBuilder for dynamic sort
    PathBuilder<?> entityPath =
        new PathBuilder<>(QContentReview.class, qContentReview.getMetadata());

    // 메인 쿼리
    JPAQuery<FlatContentReviewDetailDTO> query =
        jpaQueryFactory
            .select(
                Projections.fields(
                    FlatContentReviewDetailDTO.class,
                    qContentReview.id.as("reviewId"),
                    qContent.title.as("contentTitle"),
                    qContentReview.createdAt.as("createdAt"),
                    qUser.userProfile.nickname.as("reviewerNickname"),
                    qContentReview.reviewContent.as("reviewContent"),
                    qPurchase.selectedOptionName.as("selectedOptionName"),
                    qContentReview.rating.as("rating")))
            .from(qContentReview)
            .leftJoin(qContentReview.content, qContent)
            .leftJoin(qContentReview.user, qUser)
            .leftJoin(qContentReview.purchase, qPurchase)
            .where(cond);
    // 정렬 적용
    if (pageable.getSort().isUnsorted()) {
      query.orderBy(qContentReview.createdAt.desc());
    } else {
      for (Sort.Order order : pageable.getSort()) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC; // QueryDSL의 Order
        ComparableExpressionBase<?> expr =
            entityPath.getComparable(order.getProperty(), Comparable.class);
        query.orderBy(new OrderSpecifier<>(direction, expr));
      }
    }

    // 페이징 적용
    List<FlatContentReviewDetailDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 카운트 쿼리
    long total =
        Optional.ofNullable(
                jpaQueryFactory
                    .select(qContentReview.count())
                    .from(qContentReview)
                    .leftJoin(qContentReview.content, qContent)
                    .where(cond)
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Optional<FlatContentReviewDetailDTO> getContentReviewDetailDTO(
      Long userId, Long contentId, Long reviewId) {
    QUser qUser = QUser.user;
    QContent qContent = QContent.content;
    QPurchase qPurchase = QPurchase.purchase;
    QContentReview qContentReview = QContentReview.contentReview;

    // 기본 조건 설정 (특정 reviewId를 가진 리뷰를 찾고, 해당 리뷰가 특정 contentId에 속하고 판매자가 이를 조회했는지 확인)
    BooleanExpression conditions =
        qContentReview
            .id
            .eq(reviewId)
            .and(qContentReview.content.id.eq(contentId))
            .and(qContent.user.id.eq(userId))
            .and(qContentReview.reviewStatus.eq(ReviewStatus.ACTIVE));

    FlatContentReviewDetailDTO result =
        jpaQueryFactory
            .select(
                Projections.fields(
                    FlatContentReviewDetailDTO.class,
                    qContentReview.id.as("reviewId"),
                    qContent.title.as("contentTitle"),
                    qContentReview.createdAt.as("createdAt"),
                    qUser.userProfile.nickname.as("reviewerNickname"),
                    qContentReview.reviewContent.as("reviewContent"),
                    qPurchase.selectedOptionName.as("selectedOptionName"),
                    qContentReview.rating.as("rating")))
            .from(qContentReview)
            .leftJoin(qContentReview.content, qContent)
            .leftJoin(qContentReview.user, qUser)
            .leftJoin(qContentReview.purchase, qPurchase)
            .where(conditions)
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<FlatContentReviewDetailDTO> getContentReviewDetailDTOByContentId(
      Long userId, Long contentId) {

    QUser qUser = QUser.user;
    QContent qContent = QContent.content;
    QPurchase qPurchase = QPurchase.purchase;
    QContentReview qContentReview = QContentReview.contentReview;

    /* 1) 서브쿼리 – min() 으로 다중 row 방지 */
    Expression<String> selectedOptionNameExpr =
        ExpressionUtils.as(
            JPAExpressions.select(qPurchase.selectedOptionName.min()) // ★ 집계 함수
                .from(qPurchase)
                .where(qPurchase.user.id.eq(userId), qPurchase.content.id.eq(contentId)),
            "selectedOptionName");

    // 기본 조건 설정 (특정 reviewId를 가진 리뷰를 찾고, 해당 리뷰가 특정 contentId에 속하고 판매자가 이를 조회했는지 확인)
    BooleanExpression conditions =
        qContentReview
            .content
            .id
            .eq(contentId)
            .and(qContentReview.user.id.eq(userId))
            .and(qContentReview.reviewStatus.eq(ReviewStatus.ACTIVE)); // ← 수정

    FlatContentReviewDetailDTO result =
        jpaQueryFactory
            .select(
                Projections.fields(
                    FlatContentReviewDetailDTO.class,
                    qContentReview.id.as("reviewId"),
                    qContent.title.as("contentTitle"),
                    qContentReview.createdAt.as("createdAt"),
                    qUser.userProfile.nickname.as("reviewerNickname"),
                    qContentReview.reviewContent.as("reviewContent"),
                    selectedOptionNameExpr,
                    qContentReview.rating.as("rating")))
            .from(qContentReview)
            .leftJoin(qContentReview.content, qContent)
            .leftJoin(qContentReview.user, qUser)
            .where(conditions)
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<FlatContentReviewReplyDTO> findReviewsWithRepliesByContentId(Long contentId) {
    QContentReview qContentReview = QContentReview.contentReview;
    QContentReply qContentReply = QContentReply.contentReply;
    QUser qReviewer = new QUser("reviewer");
    QUser qSeller = new QUser("seller");
    QPurchase qPurchase = QPurchase.purchase;

    // selectedOptionName 서브쿼리
    Expression<String> selectedOptionNameExpression =
        ExpressionUtils.as(
            select(qPurchase.selectedOptionName.min()) // min() 추가
                .from(qPurchase)
                .where(
                    qPurchase.user.id.eq(qContentReview.user.id),
                    qPurchase.content.id.eq(contentId))
                .limit(1),
            "selectedOptionName");

    return jpaQueryFactory
        .select(
            Projections.fields(
                FlatContentReviewReplyDTO.class,
                // Review 정보
                qContentReview.id.as("reviewId"),
                qContentReview.createdAt.as("reviewCreatedAt"),
                qReviewer.userProfile.profileImageUrl.as("reviewerProfileImageUrl"),
                qReviewer.userProfile.nickname.as("reviewerNickname"),
                qContentReview.reviewContent.as("reviewContent"),
                selectedOptionNameExpression,
                qContentReview.rating.as("rating"),
                // Reply 정보
                qContentReply.id.as("replyId"),
                qContentReply.createdAt.as("replyCreatedAt"),
                qSeller.userProfile.nickname.as("replierNickname"),
                qContentReply.replyContent.as("replyContent")))
        .from(qContentReview)
        .leftJoin(qContentReview.user, qReviewer)
        .leftJoin(qContentReply)
        .on(
            qContentReply
                .contentReview
                .id
                .eq(qContentReview.id)
                .and(qContentReply.isDeleted.eq(false)))
        .leftJoin(qContentReply.seller, qSeller)
        .where(
            qContentReview.content.id.eq(contentId),
            qContentReview.reviewStatus.eq(ReviewStatus.ACTIVE))
        .orderBy(qContentReview.createdAt.desc(), qContentReply.createdAt.asc())
        .fetch();
  }

  @Override
  public void updateContentReviewStatusToDeleteRequested(Long userId, Long reviewId) {
    QContentReview qContentReview = QContentReview.contentReview;

    jpaQueryFactory
        .update(qContentReview)
        .set(qContentReview.reviewStatus, ReviewStatus.PENDING_DELETE)
        .set(qContentReview.deletionRequestedAt, LocalDateTime.now())
        .where(qContentReview.id.eq(reviewId).and(qContentReview.user.id.eq(userId)))
        .execute();
  }

  @Override
  public void deleteContentReview(Long userId, Long reviewId) {
    QContentReview qContentReview = QContentReview.contentReview;

    jpaQueryFactory
        .update(qContentReview)
        .set(qContentReview.reviewStatus, ReviewStatus.DELETED)
        .where(qContentReview.id.eq(reviewId).and(qContentReview.user.id.eq(userId)))
        .execute();
  }
}
