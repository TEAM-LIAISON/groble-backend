package liaison.groble.persistence.content;

import static com.querydsl.jpa.JPAExpressions.select;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.entity.QContentReview;
import liaison.groble.domain.content.repository.ContentReviewCustomRepository;
import liaison.groble.domain.purchase.entity.QPurchase;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentReviewCustomRepositoryImpl implements ContentReviewCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

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
}
