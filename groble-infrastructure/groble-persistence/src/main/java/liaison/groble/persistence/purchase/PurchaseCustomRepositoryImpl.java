package liaison.groble.persistence.purchase;

import static com.querydsl.jpa.JPAExpressions.select;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.entity.QContentOption;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.order.entity.QOrder;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.entity.QPurchase;
import liaison.groble.domain.purchase.enums.PurchaseStatus;
import liaison.groble.domain.purchase.repository.PurchaseCustomRepository;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PurchaseCustomRepositoryImpl implements PurchaseCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorResponse<FlatPurchaseContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId,
      Long lastContentId,
      int size,
      List<PurchaseStatus> statusList,
      ContentType contentType) {

    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QOrder qOrder = QOrder.order;

    // 기본 조건 설정
    BooleanExpression conditions =
        qPurchase.user.id.eq(userId).and(qContent.contentType.eq(contentType));

    // 커서 조건 추가
    if (lastContentId != null) {
      conditions = conditions.and(qContent.id.lt(lastContentId));
    }

    // 상태 필터 추가 (여러 상태 지원)
    if (statusList != null && !statusList.isEmpty()) {
      conditions = conditions.and(qPurchase.status.in(statusList));
    }

    // 조회할 개수 + 1 (다음 페이지 존재 여부 확인용)
    int fetchSize = size + 1;

    // 쿼리 실행
    List<FlatPurchaseContentPreviewDTO> results =
        queryFactory
            .select(
                Projections.fields(
                    FlatPurchaseContentPreviewDTO.class,
                    qPurchase.order.merchantUid.as("merchantUid"),
                    qContent.id.as("contentId"),
                    qPurchase.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qContent.lowestPrice.as("lowestPrice"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.status.stringValue().as("status")))
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qPurchase.order, qOrder)
            .where(conditions)
            .orderBy(qContent.id.desc())
            .limit(fetchSize)
            .fetch();

    // 다음 페이지 여부 확인
    boolean hasNext = results.size() > size;

    // 실제 반환할 리스트 조정
    List<FlatPurchaseContentPreviewDTO> items = hasNext ? results.subList(0, size) : results;

    // 다음 커서 계산
    String nextCursor = null;
    if (hasNext && !items.isEmpty()) {
      nextCursor = String.valueOf(items.get(items.size() - 1).getContentId());
    }

    // 메타데이터 (여러 상태를 표시)
    String filterValue = null;
    if (statusList != null && !statusList.isEmpty()) {
      filterValue = statusList.stream().map(PurchaseStatus::name).collect(Collectors.joining(","));
    }

    CursorResponse.MetaData meta =
        CursorResponse.MetaData.builder().filter(filterValue).cursorType("id").build();

    return CursorResponse.of(items, nextCursor, hasNext, 0, meta);
  }

  @Override
  public int countMyPurchasingContents(
      Long userId, List<PurchaseStatus> statusList, ContentType contentType) {
    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;

    // 기본 조건 설정: 사용자 ID, 콘텐츠 타입
    BooleanExpression conditions =
        qPurchase.user.id.eq(userId).and(qPurchase.content.contentType.eq(contentType));

    // 상태 필터 추가 (여러 상태 지원)
    if (statusList != null && !statusList.isEmpty()) {
      conditions = conditions.and(qPurchase.status.in(statusList));
    }

    // 쿼리 실행: Purchase 엔티티 기준으로 카운트
    Long count =
        queryFactory
            .select(qPurchase.count())
            .from(qPurchase)
            .join(qPurchase.content, qContent)
            .where(conditions)
            .fetchOne();

    return count != null ? count.intValue() : 0;
  }
}
