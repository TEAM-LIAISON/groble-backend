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
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.entity.QContentReview;
import liaison.groble.domain.content.enums.ReviewStatus;
import liaison.groble.domain.content.repository.ContentReviewCustomRepository;
import liaison.groble.domain.order.entity.QOrder;
import liaison.groble.domain.purchase.entity.QPurchase;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReviewCustomRepositoryImpl implements ContentReviewCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<ContentReview> getContentReview(Long userId, Long contentId, Long reviewId) {
    QContentReview qContentReview = QContentReview.contentReview;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    BooleanExpression conditions =
        qContentReview
            .id
            .eq(reviewId)
            .and(qContentReview.content.id.eq(contentId))
            .and(qContent.user.id.eq(userId))
            .and(qContentReview.user.id.eq(qUser.id));

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
        qContentReview.content.id.eq(contentId).and(qContent.user.id.eq(userId));

    // selectedOptionName 서브쿼리
    Expression<String> selectedOptionNameExpression =
        ExpressionUtils.as(
            select(qPurchase.selectedOptionName)
                .from(qPurchase)
                .where(
                    qPurchase.user.id.eq(qContentReview.user.id),
                    qPurchase.content.id.eq(contentId))
                .limit(1),
            "selectedOptionName");

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
                    selectedOptionNameExpression,
                    qContentReview.rating.as("rating")))
            .from(qContentReview)
            .leftJoin(qContentReview.content, qContent)
            .leftJoin(qContentReview.user, qUser)
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

    Expression<String> selectedOptionNameExpression =
        ExpressionUtils.as(
            select(qPurchase.selectedOptionName)
                .from(qPurchase)
                .where(qPurchase.user.id.eq(userId), qPurchase.content.id.eq(contentId))
                .limit(1),
            "selectedOptionName");

    // 기본 조건 설정 (특정 reviewId를 가진 리뷰를 찾고, 해당 리뷰가 특정 contentId에 속하고 판매자가 이를 조회했는지 확인)
    BooleanExpression conditions =
        qContentReview
            .id
            .eq(reviewId)
            .and(qContentReview.content.id.eq(contentId))
            .and(qContent.user.id.eq(userId));

    FlatContentReviewDetailDTO result =
        jpaQueryFactory
            .select(
                Projections.fields(
                    FlatContentReviewDetailDTO.class,
                    qContentReview.id.as("reviewId"),
                    qContent.title.as("contentTitle"),
                    qContentReview.createdAt.as("createdAt"),
                    qUser.userProfile.nickname.as("reviewerNickname"),
                    selectedOptionNameExpression,
                    qContentReview.rating.as("rating")))
            .from(qContentReview)
            .leftJoin(qContentReview.content, qContent)
            .leftJoin(qContentReview.user, qUser)
            .where(conditions)
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<FlatContentReviewDetailDTO> getContentReviewDetailDTOByMerchantUid(
      Long userId, String merchantUid) {
    QUser qUser = QUser.user;
    QContent qContent = QContent.content;
    QPurchase qPurchase = QPurchase.purchase;
    QOrder qOrder = qPurchase.order;
    QContentReview qContentReview = QContentReview.contentReview;

    /* 서브쿼리: 구매자가 어떤 옵션을 골랐는지 */
    Expression<String> selectedOptionNameExpression =
        ExpressionUtils.as(
            JPAExpressions.select(qPurchase.selectedOptionName)
                .from(qPurchase)
                .join(qPurchase.order, qOrder) // ★ Purchase → Order 조인
                .where(
                    qPurchase.user.id.eq(userId), // 구매자 ID
                    qOrder.merchantUid.eq(merchantUid) // Order.merchantUid
                    )
                .limit(1),
            "selectedOptionName");

    /* 메인 조건 */
    BooleanExpression conditions =
        qOrder
            .merchantUid
            .eq(merchantUid) // ★ Order 기준으로 merchantUid 필터
            .and(qContent.user.id.eq(userId)); // 판매자/리뷰어 조건(필요에 따라 수정)

    FlatContentReviewDetailDTO result =
        jpaQueryFactory
            .select(
                Projections.fields(
                    FlatContentReviewDetailDTO.class,
                    qContentReview.id.as("reviewId"),
                    qContent.title.as("contentTitle"),
                    qContentReview.createdAt.as("createdAt"),
                    qUser.userProfile.nickname.as("reviewerNickname"),
                    selectedOptionNameExpression,
                    qContentReview.rating.as("rating")))
            .from(qContentReview)
            .leftJoin(qContentReview.content, qContent)
            .leftJoin(qContentReview.user, qUser)
            .leftJoin(qPurchase.order, qOrder) // ★
            .where(conditions)
            .fetchOne();

    return Optional.ofNullable(result);
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
