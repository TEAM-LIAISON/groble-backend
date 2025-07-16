package liaison.groble.persistence.purchase;

import static com.querydsl.jpa.JPAExpressions.select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.entity.QContentOption;
import liaison.groble.domain.content.entity.QContentReview;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.QOrder;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.dto.FlatSellManageDetailDTO;
import liaison.groble.domain.purchase.entity.QPurchase;
import liaison.groble.domain.purchase.repository.PurchaseCustomRepository;
import liaison.groble.domain.user.entity.QIntegratedAccount;
import liaison.groble.domain.user.entity.QSocialAccount;
import liaison.groble.domain.user.entity.QUser;
import liaison.groble.domain.user.enums.AccountType;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PurchaseCustomRepositoryImpl implements PurchaseCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorResponse<FlatPurchaseContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, List<Order.OrderStatus> statusList) {

    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QOrder qOrder = QOrder.order;

    // 기본 조건 설정
    BooleanExpression conditions = qPurchase.user.id.eq(userId);

    // 커서 조건 추가
    if (lastContentId != null) {
      conditions = conditions.and(qContent.id.lt(lastContentId));
    }

    // 상태 필터 추가 (여러 상태 지원)
    if (statusList != null && !statusList.isEmpty()) {
      conditions = conditions.and(qOrder.status.in(statusList));
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
                    qContent.contentType.stringValue().as("contentType"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qContent.user.userProfile.nickname.as("sellerName"),
                    qPurchase.originalPrice.as("originalPrice"),
                    qPurchase.finalPrice.as("finalPrice"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qOrder.status.stringValue().as("orderStatus"),
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
      filterValue =
          statusList.stream().map(Order.OrderStatus::name).collect(Collectors.joining(","));
    }

    CursorResponse.MetaData meta =
        CursorResponse.MetaData.builder().filter(filterValue).cursorType("id").build();

    return CursorResponse.of(items, nextCursor, hasNext, 0, meta);
  }

  @Override
  public int countMyPurchasingContents(Long userId, List<Order.OrderStatus> statusList) {
    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QOrder qOrder = QOrder.order;

    // 기본 조건 설정: 사용자 ID, 콘텐츠 타입
    BooleanExpression conditions = qPurchase.user.id.eq(userId);

    // 상태 필터 추가 (여러 상태 지원)
    if (statusList != null && !statusList.isEmpty()) {
      conditions = conditions.and(qOrder.status.in(statusList));
    }

    // 쿼리 실행: Purchase 엔티티 기준으로 카운트
    Long count =
        queryFactory
            .select(qPurchase.count())
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .where(conditions)
            .fetchOne();

    return count != null ? count.intValue() : 0;
  }

  @Override
  public Optional<FlatContentSellDetailDTO> getContentSellDetailDTO(
      Long userId, Long contentId, Long purchaseId) {
    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QIntegratedAccount qIntegratedAccount = QIntegratedAccount.integratedAccount;
    QSocialAccount qSocialAccount = QSocialAccount.socialAccount;

    BooleanExpression conditions =
        qContent.id.eq(contentId).and(qContent.user.id.eq(userId)).and(qPurchase.id.eq(purchaseId));

    FlatContentSellDetailDTO result =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentSellDetailDTO.class,
                    qPurchase.id.as("purchaseId"),
                    qContent.title.as("title"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qUser.userProfile.nickname.as("purchaserNickname"),
                    // 조건부 이메일 처리
                    Expressions.cases()
                        .when(qUser.accountType.eq(AccountType.INTEGRATED))
                        .then(qIntegratedAccount.integratedAccountEmail)
                        .when(qUser.accountType.eq(AccountType.SOCIAL))
                        .then(qSocialAccount.socialAccountEmail)
                        .otherwise(Expressions.nullExpression(String.class))
                        .as("purchaserEmail"),
                    qUser.userProfile.phoneNumber.as("purchaserPhoneNumber"),
                    qPurchase.selectedOptionName.as("selectedOptionName"),
                    qPurchase.finalPrice.as("finalPrice")))
            .from(qPurchase)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qUser.integratedAccount, qIntegratedAccount)
            .leftJoin(qUser.socialAccount, qSocialAccount)
            .leftJoin(qPurchase.content, qContent)
            .where(conditions)
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Page<FlatContentSellDetailDTO> getContentSellPageDTOs(
      Long userId, Long contentId, Pageable pageable) {

    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QIntegratedAccount qIntegratedAcc = QIntegratedAccount.integratedAccount;
    QSocialAccount qSocialAcc = QSocialAccount.socialAccount;

    // 조건: contentId + 소유자
    BooleanExpression conditions =
        qPurchase.content.id.eq(contentId).and(qPurchase.content.user.id.eq(userId));

    JPAQuery<FlatContentSellDetailDTO> query =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentSellDetailDTO.class,
                    qPurchase.id.as("purchaseId"),
                    qContent.title.as("contentTitle"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qUser.userProfile.nickname.as("purchaserNickname"),
                    // 이메일
                    Expressions.cases()
                        .when(qUser.accountType.eq(AccountType.INTEGRATED))
                        .then(qIntegratedAcc.integratedAccountEmail)
                        .when(qUser.accountType.eq(AccountType.SOCIAL))
                        .then(qSocialAcc.socialAccountEmail)
                        .otherwise(Expressions.nullExpression(String.class))
                        .as("purchaserEmail"),
                    qUser.userProfile.phoneNumber.as("purchaserPhoneNumber"),
                    qPurchase.selectedOptionName.as("selectedOptionName"),
                    qPurchase.finalPrice.as("finalPrice")))
            .from(qPurchase)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qUser.integratedAccount, qIntegratedAcc)
            .leftJoin(qUser.socialAccount, qSocialAcc)
            .leftJoin(qPurchase.content, qContent)
            .where(conditions);

    // ─── 1) 기본 정렬 ─────────────────────────────
    if (pageable.getSort().isUnsorted()) {
      query.orderBy(qPurchase.purchasedAt.desc());
    } else {
      // ─── 2) dynamic sort ──────────────────────────
      for (Sort.Order sortOrder : pageable.getSort()) {
        com.querydsl.core.types.Order direction =
            sortOrder.isAscending()
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        String property = sortOrder.getProperty();
        if ("purchasedAt".equals(property)) {
          // Purchase.purchasedAt 으로 직접 참조
          query.orderBy(new OrderSpecifier<>(direction, qPurchase.purchasedAt));
        } else {
          PathBuilder<Content> contentPath =
              new PathBuilder<>(Content.class, qContent.getMetadata());
          ComparableExpressionBase<?> expr = contentPath.getComparable(property, Comparable.class);
          query.orderBy(new OrderSpecifier<>(direction, expr));
        }
      }
    }

    // ─── 페이징, 조회, count ───────────────────────
    List<FlatContentSellDetailDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    long total =
        Optional.ofNullable(
                queryFactory.select(qPurchase.count()).from(qPurchase).where(conditions).fetchOne())
            .orElse(0L);

    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Page<FlatPurchaseContentPreviewDTO> findMyPurchasedContents(
      Long userId, List<Order.OrderStatus> orderStatuses, Pageable pageable) {
    QPurchase qPurchase = QPurchase.purchase;
    QOrder qOrder = QOrder.order;
    QContent qContent = QContent.content;
    QContentOption qContentOption = QContentOption.contentOption;
    QUser qUser = QUser.user;

    BooleanExpression conditions = qPurchase.user.id.eq(userId);
    if (orderStatuses != null) {
      conditions = conditions.and(qOrder.status.in(orderStatuses));
    }

    JPAQuery<FlatPurchaseContentPreviewDTO> query =
        queryFactory
            .select(
                Projections.fields(
                    FlatPurchaseContentPreviewDTO.class,
                    qOrder.merchantUid.as("merchantUid"),
                    qContent.id.as("contentId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qContent.user.userProfile.nickname.as("sellerName"),
                    qPurchase.originalPrice.as("originalPrice"),
                    qPurchase.finalPrice.as("finalPrice"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qOrder.status.stringValue().as("orderStatus")))
            .from(qPurchase)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .where(conditions);

    // 3) Pageable의 Sort 적용
    if (pageable.getSort().isUnsorted()) {
      query.orderBy(qPurchase.purchasedAt.desc()); // qContent -> qPurchase
    } else {
      for (Sort.Order order : pageable.getSort()) {
        com.querydsl.core.types.Order direction =
            order.isAscending()
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        // purchasedAt은 Purchase 엔티티에서 처리
        if ("purchasedAt".equals(order.getProperty())) {
          query.orderBy(new OrderSpecifier<>(direction, qPurchase.purchasedAt));
        } else {
          // 다른 필드는 Content에서 처리
          PathBuilder<Content> path = new PathBuilder<>(Content.class, qContent.getMetadata());
          ComparableExpressionBase<?> expr =
              path.getComparable(order.getProperty(), Comparable.class);
          query.orderBy(new OrderSpecifier<>(direction, expr));
        }
      }
    }

    // 4) 페이징(Offset + Limit)
    List<FlatPurchaseContentPreviewDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 5) 전체 카운트
    long total =
        Optional.ofNullable(
                queryFactory
                    .select(qPurchase.count())
                    .from(qPurchase)
                    .leftJoin(qPurchase.order, qOrder) // 이 라인 추가
                    .where(conditions)
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Optional<FlatSellManageDetailDTO> getSellManageDetail(Long userId, Long contentId) {
    QPurchase qPurchase = QPurchase.purchase;
    QOrder qOrder = QOrder.order;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentReview qContentReview = QContentReview.contentReview;

    BooleanExpression conditions =
        qPurchase
            .content
            .id
            .eq(contentId)
            .and(qPurchase.content.user.id.eq(userId))
            .and(qOrder.status.eq(Order.OrderStatus.PAID));

    FlatSellManageDetailDTO result =
        queryFactory
            .select(
                Projections.constructor(
                    FlatSellManageDetailDTO.class,
                    // BigDecimal 그대로 전달 (데이터 없으면 0)
                    qPurchase.finalPrice.sum().coalesce(BigDecimal.ZERO),
                    // Long으로, 데이터 없으면 0
                    qPurchase.user.id.countDistinct().coalesce(0L),
                    // 서브쿼리 count() 결과도 Long, 데이터 없으면 0
                    JPAExpressions.select(qContentReview.count().coalesce(0L))
                        .from(qContentReview)
                        .where(qContentReview.content.id.eq(contentId))))
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qPurchase.order, qOrder)
            .where(
                qPurchase
                    .content
                    .id
                    .eq(contentId)
                    .and(qPurchase.content.user.id.eq(userId))
                    .and(qOrder.status.eq(Order.OrderStatus.PAID)))
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public boolean existsByUserAndContent(Long userId, Long contentId) {

    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    BooleanExpression conditions =
        qPurchase.user.id.eq(userId).and(qPurchase.content.id.eq(contentId));
    return queryFactory
            .selectOne()
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .where(conditions)
            .fetchFirst()
        != null;
  }
}
