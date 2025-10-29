package liaison.groble.persistence.content;

import static com.querydsl.jpa.JPAExpressions.select;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatAdminContentSummaryInfoDTO;
import liaison.groble.domain.content.dto.FlatContentOverviewDTO;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.dto.FlatDynamicContentDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.QCategory;
import liaison.groble.domain.content.entity.QCoachingOption;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.entity.QContentOption;
import liaison.groble.domain.content.entity.QDocumentOption;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.purchase.entity.QPurchase;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ContentCustomRepositoryImpl implements ContentCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<FlatContentPreviewDTO> findRepresentativeContentByUser(Long userId) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    FlatContentPreviewDTO result =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.status.stringValue().as("status"),
                    qContent.isSearchExposed.as("isSearchExposed")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .leftJoin(qContent.category, qCategory)
            .where(qContent.user.id.eq(userId).and(qContent.isRepresentative.isTrue()))
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<FlatContentPreviewDTO> findFlatContentById(Long contentId) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    FlatContentPreviewDTO result =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.status.stringValue().as("status"),
                    qContent.isSearchExposed.as("isSearchExposed")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .leftJoin(qContent.category, qCategory)
            .where(qContent.id.eq(contentId))
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<FlatContentPreviewDTO> findFlatContentsByUserId(Long userId) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    return queryFactory
        .select(
            Projections.fields(
                FlatContentPreviewDTO.class,
                qContent.id.as("contentId"),
                qContent.createdAt.as("createdAt"),
                qContent.title.as("title"),
                qContent.thumbnailUrl.as("thumbnailUrl"),
                qUser.userProfile.nickname.as("sellerName"),
                qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                qCategory.code.as("categoryId"),
                qContent.contentType.stringValue().as("contentType"),
                qContent.paymentType.stringValue().as("paymentType"),
                ExpressionUtils.as(
                    select(qContentOption.count().intValue())
                        .from(qContentOption)
                        .where(qContentOption.content.eq(qContent)),
                    "priceOptionLength"),
                qContent.status.stringValue().as("status"),
                qContent.isSearchExposed.as("isSearchExposed")))
        .from(qContent)
        .leftJoin(qContent.user, qUser)
        .leftJoin(qContent.category, qCategory)
        .where(qContent.user.id.eq(userId))
        .fetch();
  }

  @Override
  public List<FlatContentPreviewDTO> findHomeContents() {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    BooleanExpression conditions =
        qContent.status.eq(ContentStatus.ACTIVE).and(qContent.sortOrder.ne(0));

    return queryFactory
        .select(
            Projections.fields(
                FlatContentPreviewDTO.class,
                qContent.id.as("contentId"),
                qContent.createdAt.as("createdAt"),
                qContent.title.as("title"),
                qContent.thumbnailUrl.as("thumbnailUrl"),
                qUser.userProfile.nickname.as("sellerName"),
                qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                qContent.lowestPrice.as("lowestPrice"),
                qCategory.code.as("categoryId"),
                qContent.contentType.stringValue().as("contentType"),
                qContent.paymentType.stringValue().as("paymentType"),
                ExpressionUtils.as(
                    select(qContentOption.count().intValue())
                        .from(qContentOption)
                        .where(qContentOption.content.eq(qContent)),
                    "priceOptionLength"),
                qContent.status.stringValue().as("status"),
                qContent.isSearchExposed.as("isSearchExposed")))
        .from(qContent)
        .leftJoin(qContent.user, qUser)
        .leftJoin(qContent.category, qCategory)
        .where(conditions)
        // --- sortOrder DESC를 가장 먼저 두고, 동일한 sortOrder끼리는 createdAt 기준으로 최신순 정렬 ---
        .orderBy(qContent.sortOrder.desc(), qContent.createdAt.desc())
        .fetch();
  }

  @Override
  public List<FlatContentPreviewDTO> findAllMarketContentsByUserId(Long userId) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QDocumentOption qDocOpt = QDocumentOption.documentOption;
    QCategory qCategory = QCategory.category;

    // isRepresentative.isFalse() 조건 제거
    BooleanExpression condition =
        qContent.status.eq(ContentStatus.ACTIVE).and(qContent.user.id.eq(userId));

    // 2) 콘텐츠 필드 유효 검사
    BooleanExpression contentValid =
        qContent
            .title
            .isNotNull()
            .and(qContent.thumbnailUrl.isNotNull())
            .and(qContent.lowestPrice.isNotNull());

    log.info("Content Validity Check: {}", contentValid);

    // 3) 문서 옵션 존재 여부 (file OR link 중 하나라도 있으면 true)
    BooleanExpression hasValidDocOpt =
        JPAExpressions.selectOne()
            .from(qDocOpt)
            .where(
                qDocOpt.content.eq(qContent),
                // fileUrl 이 NOT NULL 이거나 linkUrl 이 NOT NULL 이면
                qDocOpt.documentFileUrl.isNotNull().or(qDocOpt.documentLinkUrl.isNotNull()))
            .exists();

    log.info("Has Valid DocOpt Check: {}", hasValidDocOpt);

    // 5) 판매 가능 여부 식
    Expression<Boolean> availableForSale =
        new CaseBuilder().when(contentValid.and(hasValidDocOpt)).then(true).otherwise(false);

    log.info("Available For Sale Check: {}", availableForSale);

    return queryFactory
        .select(
            Projections.fields(
                FlatContentPreviewDTO.class,
                qContent.id.as("contentId"),
                qContent.createdAt.as("createdAt"),
                qContent.title.as("title"),
                qContent.thumbnailUrl.as("thumbnailUrl"),
                qUser.userProfile.nickname.as("sellerName"),
                qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                qContent.lowestPrice.as("lowestPrice"),
                qCategory.code.as("categoryId"),
                qContent.contentType.stringValue().as("contentType"),
                qContent.paymentType.stringValue().as("paymentType"),
                ExpressionUtils.as(
                    select(qContentOption.count().intValue())
                        .from(qContentOption)
                        .where(qContentOption.content.eq(qContent)),
                    "priceOptionLength"),
                ExpressionUtils.as(availableForSale, "isAvailableForSale"),
                qContent.status.stringValue().as("status"),
                qContent.isSearchExposed.as("isSearchExposed")))
        .from(qContent)
        .leftJoin(qContent.user, qUser)
        .leftJoin(qContent.category, qCategory)
        .where(condition)
        .orderBy(
            // 대표 콘텐츠를 먼저 (true = 1, false = 0으로 정렬)
            qContent.isRepresentative.desc(),
            // 그 다음 생성일자 역순
            qContent.createdAt.desc())
        .fetch();
  }

  @Override
  public CursorResponse<FlatContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, ContentStatus status, ContentType contentType) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    // 기본 조건 설정
    BooleanExpression conditions =
        qContent.user.id.eq(userId).and(qContent.contentType.eq(contentType));

    // 커서 조건 추가
    if (lastContentId != null) {
      conditions = conditions.and(qContent.id.lt(lastContentId));
    }

    // 상태 필터 추가
    if (status != null) {
      conditions = conditions.and(qContent.status.eq(status));
    }

    // 조회할 개수 + 1 (다음 페이지 존재 여부 확인용)
    int fetchSize = size + 1;

    // 쿼리 실행
    List<FlatContentPreviewDTO> results =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.status.stringValue().as("status"),
                    qContent.isSearchExposed.as("isSearchExposed")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .leftJoin(qContent.category, qCategory)
            .where(conditions)
            .orderBy(qContent.id.desc())
            .limit(fetchSize)
            .fetch();

    // 다음 페이지 여부 확인
    boolean hasNext = results.size() > size;

    // 실제 반환할 리스트 조정
    List<FlatContentPreviewDTO> items = hasNext ? results.subList(0, size) : results;

    // 다음 커서 계산
    String nextCursor = null;
    if (hasNext && !items.isEmpty()) {
      nextCursor = String.valueOf(items.get(items.size() - 1).getContentId());
    }

    // 메타데이터
    CursorResponse.MetaData meta =
        CursorResponse.MetaData.builder()
            .filter(status != null ? status.name() : null)
            .cursorType("id")
            .build();

    return CursorResponse.of(items, nextCursor, hasNext, 0, meta);
  }

  @Override
  public CursorResponse<FlatContentPreviewDTO> findMySellingContentsWithCursor(
      Long userId, Long lastContentId, int size, List<ContentStatus> statusList) {

    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    // 기본 조건 설정
    BooleanExpression conditions = qContent.user.id.eq(userId);

    // 커서 조건 추가
    if (lastContentId != null) {
      conditions = conditions.and(qContent.id.lt(lastContentId));
    }

    // 상태 필터 추가 (여러 상태 지원)
    if (statusList != null && !statusList.isEmpty()) {
      conditions = conditions.and(qContent.status.in(statusList));
    }

    // 조회할 개수 + 1 (다음 페이지 존재 여부 확인용)
    int fetchSize = size + 1;

    // 쿼리 실행
    List<FlatContentPreviewDTO> results =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.status.stringValue().as("status"),
                    qContent.isSearchExposed.as("isSearchExposed")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .leftJoin(qContent.category, qCategory)
            .where(conditions)
            .orderBy(qContent.id.desc())
            .limit(fetchSize)
            .fetch();

    // 다음 페이지 여부 확인
    boolean hasNext = results.size() > size;

    // 실제 반환할 리스트 조정
    List<FlatContentPreviewDTO> items = hasNext ? results.subList(0, size) : results;

    // 다음 커서 계산
    String nextCursor = null;
    if (hasNext && !items.isEmpty()) {
      nextCursor = String.valueOf(items.get(items.size() - 1).getContentId());
    }

    // 메타데이터 (여러 상태를 표시)
    String filterValue =
        Objects.requireNonNull(statusList).stream()
            .map(ContentStatus::name)
            .collect(Collectors.joining(","));

    CursorResponse.MetaData meta =
        CursorResponse.MetaData.builder().filter(filterValue).cursorType("id").build();

    return CursorResponse.of(items, nextCursor, hasNext, 0, meta);
  }

  @Override
  public CursorResponse<FlatContentPreviewDTO> findHomeContentsWithCursor(
      Long lastContentId, int size, ContentType contentType) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    // 기본 조건 설정
    BooleanExpression conditions = qContent.contentType.eq(contentType);
    // 커서 조건 추가
    if (lastContentId != null) {
      conditions = conditions.and(qContent.id.lt(lastContentId));
    }

    // 조회할 개수 + 1 (다음 페이지 존재 여부 확인용)
    int fetchSize = size + 1;

    // 쿼리 실행
    List<FlatContentPreviewDTO> results =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.status.stringValue().as("status"),
                    qContent.isSearchExposed.as("isSearchExposed")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .leftJoin(qContent.category, qCategory)
            .where(conditions)
            .orderBy(qContent.id.desc())
            .limit(fetchSize)
            .fetch();
    // 다음 페이지 여부 확인
    boolean hasNext = results.size() > size;
    // 실제 반환할 리스트 조정
    List<FlatContentPreviewDTO> items = hasNext ? results.subList(0, size) : results;
    // 다음 커서 계산
    String nextCursor = null;
    if (hasNext && !items.isEmpty()) {
      nextCursor = String.valueOf(items.get(items.size() - 1).getContentId());
    }

    return CursorResponse.of(items, nextCursor, hasNext, 0);
  }

  @Override
  public int countMySellingContents(Long userId, List<ContentStatus> statusList) {
    QContent qContent = QContent.content;

    // 기본 조건 설정
    BooleanExpression conditions = qContent.user.id.eq(userId);

    // 상태 필터 추가 (여러 상태 지원)
    if (statusList != null && !statusList.isEmpty()) {
      conditions = conditions.and(qContent.status.in(statusList));
    }

    // 결과가 null일 경우를 대비한 안전한 처리
    Long count = queryFactory.select(qContent.count()).from(qContent).where(conditions).fetchOne();

    // null 체크 후 반환 (결과가 null이면 0 반환)
    return count != null ? count.intValue() : 0;
  }

  @Override
  public int countMyPurchasingContents(Long userId, ContentStatus status, ContentType contentType) {
    QContent qContent = QContent.content;

    // 기본 조건 설정
    BooleanExpression conditions =
        qContent.user.id.eq(userId).and(qContent.contentType.eq(contentType));

    // 상태 필터 추가
    if (status != null) {
      conditions = conditions.and(qContent.status.eq(status));
    }

    // 결과가 null일 경우를 대비한 안전한 처리
    Long count = queryFactory.select(qContent.count()).from(qContent).where(conditions).fetchOne();

    // null 체크 후 반환 (결과가 null이면 0 반환)
    return count != null ? count.intValue() : 0;
  }

  @Override
  public Page<FlatContentPreviewDTO> findContentsByType(
      ContentType contentType, Pageable pageable) {

    QContent q = QContent.content;
    QUser u = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    // 1) 기본 조건
    BooleanExpression cond = q.contentType.eq(contentType).and(q.status.eq(ContentStatus.ACTIVE));

    // 2) QueryDSL 쿼리 빌드
    JPAQuery<FlatContentPreviewDTO> query =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    q.id.as("contentId"),
                    q.createdAt.as("createdAt"),
                    q.title.as("title"),
                    q.thumbnailUrl.as("thumbnailUrl"),
                    u.userProfile.nickname.as("sellerName"),
                    q.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    q.contentType.stringValue().as("contentType"),
                    q.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(q)),
                        "priceOptionLength"),
                    q.status.stringValue().as("status"),
                    q.isSearchExposed.as("isSearchExposed")))
            .from(q)
            .leftJoin(q.user, u)
            .leftJoin(q.category, qCategory)
            .where(cond);

    // 3) Sort 적용
    if (pageable.getSort().isUnsorted()) {
      // 기본: 생성일 내림차순
      query.orderBy(q.createdAt.desc());
    } else {
      // 동적 정렬: createdAt, popular(viewCount) 등
      PathBuilder<Content> path = new PathBuilder<>(Content.class, q.getMetadata());
      for (Sort.Order order : pageable.getSort()) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;
        String prop = order.getProperty();

        if ("popular".equalsIgnoreCase(prop)) {
          // viewCount 기준 정렬
          NumberExpression<Long> viewExpr = path.getNumber("viewCount", Long.class);
          query.orderBy(new OrderSpecifier<>(direction, viewExpr));
        } else {
          // 그 외 필드(prop) 기준 정렬
          ComparableExpressionBase<?> expr = path.getComparable(prop, Comparable.class);
          query.orderBy(new OrderSpecifier<>(direction, expr));
        }
      }
    }

    // 4) 페이징
    List<FlatContentPreviewDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 5) total count
    Long total = queryFactory.select(q.count()).from(q).where(cond).fetchOne();

    return new PageImpl<>(items, pageable, total != null ? total : 0L);
  }

  @Override
  public Page<FlatContentPreviewDTO> findAllMarketContentsByUserIdWithPaging(
      Long userId, Pageable pageable) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;

    // 1) 기본 조건
    BooleanExpression condition =
        qContent
            .status
            .eq(ContentStatus.ACTIVE)
            .and(qContent.isRepresentative.isFalse())
            .and(qContent.user.id.eq(userId));

    JPAQuery<FlatContentPreviewDTO> query =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.status.stringValue().as("status"),
                    qContent.isSearchExposed.as("isSearchExposed")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .leftJoin(qContent.category, qCategory)
            .where(condition);

    // 3) Pageable의 Sort 적용 (여기서는 예시로 createdAt 기준)
    if (pageable.getSort().isUnsorted()) {
      query.orderBy(qContent.createdAt.desc());
    } else {
      // qContent 는 QContent.content
      PathBuilder<Content> path = new PathBuilder<>(Content.class, qContent.getMetadata());

      // Sort.Order 순회
      for (Sort.Order order : pageable.getSort()) {
        // ASC / DESC
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        // ComparableExpression 으로 꺼내오기
        // (모든 필드를 Comparable 으로 가정)
        ComparableExpressionBase<?> expr =
            path.getComparable(order.getProperty(), Comparable.class);

        // 이제 Expression 타입이 맞아서 컴파일 OK
        query.orderBy(new OrderSpecifier<>(direction, expr));
      }
    }

    // 4) 페이징(Offset + Limit)
    List<FlatContentPreviewDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 5) 전체 카운트
    long total =
        Optional.ofNullable(
                queryFactory.select(qContent.count()).from(qContent).where(condition).fetchOne())
            .orElse(0L);
    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Page<FlatContentPreviewDTO> findContentsByCategoriesAndType(
      List<String> categoryCodes, ContentType type, Pageable pageable) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QCategory qCategory = QCategory.category;
    // 1) 기본 조건
    BooleanExpression cond =
        qContent
            .contentType
            .eq(type)
            .and(qContent.status.eq(ContentStatus.ACTIVE))
            .and(qContent.category.code.in(categoryCodes));

    // 2) QueryDSL 쿼리 빌드
    JPAQuery<FlatContentPreviewDTO> query =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.status.stringValue().as("status"),
                    qContent.isSearchExposed.as("isSearchExposed")))
            .from(qContent)
            .leftJoin(qContent.user, qUser) // 사용자 정보는 반드시 필요하니까 남겨두고
            .leftJoin(qContent.category, qCategory)
            .where(cond);

    // 3) Pageable의 Sort 적용 (여기서는 예시로 createdAt 기준)
    if (pageable.getSort().isUnsorted()) {
      query.orderBy(qContent.createdAt.desc());
    } else {
      // qContent 는 QContent.content
      PathBuilder<Content> path = new PathBuilder<>(Content.class, qContent.getMetadata());

      // Sort.Order 순회
      for (Sort.Order order : pageable.getSort()) {
        // ASC / DESC
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        // ComparableExpression 으로 꺼내오기
        // (모든 필드를 Comparable 으로 가정)
        ComparableExpressionBase<?> expr =
            path.getComparable(order.getProperty(), Comparable.class);

        // 이제 Expression 타입이 맞아서 컴파일 OK
        query.orderBy(new OrderSpecifier<>(direction, expr));
      }
    }

    // 4) 페이징(Offset + Limit)
    List<FlatContentPreviewDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 5) 전체 카운트
    long total =
        Optional.ofNullable(
                queryFactory.select(qContent.count()).from(qContent).where(cond).fetchOne())
            .orElse(0L);
    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public List<FlatDynamicContentDTO> findAllDynamicContents() {
    QContent qContent = QContent.content;

    BooleanExpression cond = qContent.status.eq(ContentStatus.ACTIVE);

    return queryFactory
        .select(
            Projections.constructor(
                FlatDynamicContentDTO.class,
                qContent.id.as("contentId"),
                qContent.title.as("title"),
                qContent.contentType.stringValue().as("contentType"),
                qContent.thumbnailUrl.as("thumbnailUrl"),
                qContent.updatedAt.as("updatedAt")))
        .from(qContent)
        .where(cond)
        .orderBy(qContent.id.desc())
        .fetch();
  }

  @Override
  public Page<FlatAdminContentSummaryInfoDTO> findContentsByPageable(Pageable pageable) {
    QContent qContent = QContent.content;
    QContentOption qContentOption = QContentOption.contentOption;
    // Order를 기준으로 조회하도록 변경
    JPAQuery<FlatAdminContentSummaryInfoDTO> query =
        queryFactory
            .select(
                Projections.constructor(
                    FlatAdminContentSummaryInfoDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.user.userProfile.nickname.as("sellerName"),
                    qContent.title.as("contentTitle"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qContent.lowestPrice.as("minPrice"),
                    qContent.status.stringValue().as("contentStatus"),
                    qContent
                        .adminContentCheckingStatus
                        .stringValue()
                        .as("adminContentCheckingStatus"),
                    qContent.isSearchExposed))
            .from(qContent)
            .orderBy(qContent.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    List<FlatAdminContentSummaryInfoDTO> content = query.fetch();

    // 동일한 조건으로 전체 카운트 조회
    Long total = queryFactory.select(qContent.count()).from(qContent).fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }

  @Override
  public boolean existsSellingContentByUser(Long userId) {
    QContent qContent = QContent.content;
    return queryFactory
            .selectOne()
            .from(qContent)
            .where(qContent.user.id.eq(userId).and(qContent.status.eq(ContentStatus.ACTIVE)))
            .fetchFirst()
        != null;
  }

  @Override
  public Page<FlatContentPreviewDTO> findMyContentsWithStatus(
      Pageable pageable, Long userId, List<ContentStatus> statuses) {

    QContent qContent = QContent.content;
    QContentOption qOption = QContentOption.contentOption;
    QDocumentOption qDocOpt = QDocumentOption.documentOption;
    QCoachingOption qCoachOpt = QCoachingOption.coachingOption;
    QUser qUser = QUser.user;
    QPurchase qPurchase = QPurchase.purchase;
    QCategory qCategory = QCategory.category;

    // 1) 상태 + 사용자 필터
    BooleanExpression statusFilter =
        (statuses != null && !statuses.isEmpty()) ? qContent.status.in(statuses) : null;
    BooleanExpression userFilter = qContent.user.id.eq(userId);
    BooleanExpression whereClause =
        statusFilter != null ? userFilter.and(statusFilter) : userFilter;

    // 2) 콘텐츠 필드 유효 검사 (isAvailableForSale과 동일)
    BooleanExpression contentValid =
        qContent
            .thumbnailUrl
            .isNotNull()
            .and(qContent.status.eq(ContentStatus.DRAFT))
            .and(qContent.title.isNotNull())
            .and(qContent.makerIntro.isNotNull())
            .and(qContent.serviceProcess.isNotNull())
            .and(qContent.serviceTarget.isNotNull());

    // 3) DOCUMENT 옵션 검사
    BooleanExpression hasValidDocOpt =
        JPAExpressions.selectOne()
            .from(qDocOpt)
            .where(
                qDocOpt.content.eq(qContent),
                qDocOpt.name.isNotNull(),
                qDocOpt.description.isNotNull(),
                qDocOpt
                    .documentFileUrl
                    .isNotNull()
                    .and(qDocOpt.documentOriginalFileName.isNotNull())
                    .or(qDocOpt.documentLinkUrl.isNotNull()))
            .exists();

    // 4) COACHING 옵션 검사
    BooleanExpression hasValidCoachingOpt =
        JPAExpressions.selectOne()
            .from(qCoachOpt)
            .where(
                qCoachOpt.content.eq(qContent),
                qCoachOpt.name.isNotNull(),
                qCoachOpt.description.isNotNull())
            .exists();

    // 5) ContentType별 판매 가능 여부
    Expression<Boolean> availableForSale =
        new CaseBuilder()
            .when(
                qContent.contentType.eq(ContentType.DOCUMENT).and(contentValid).and(hasValidDocOpt))
            .then(true)
            .when(
                qContent
                    .contentType
                    .eq(ContentType.COACHING)
                    .and(contentValid)
                    .and(hasValidCoachingOpt))
            .then(true)
            .otherwise(false);

    // 6) 삭제 가능 여부
    Expression<Boolean> isDeletableExpr =
        new CaseBuilder()
            .when(
                JPAExpressions.selectOne()
                    .from(qPurchase)
                    .where(qPurchase.content.eq(qContent))
                    .exists())
            .then(false)
            .otherwise(true);

    // 7) 메인 쿼리
    JPAQuery<FlatContentPreviewDTO> query =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.userProfile.nickname.as("sellerName"),
                    qUser.userProfile.profileImageUrl.as("sellerProfileImageUrl"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qCategory.code.as("categoryId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    ExpressionUtils.as(
                        JPAExpressions.select(qOption.count().intValue())
                            .from(qOption)
                            .where(qOption.content.eq(qContent)),
                        "priceOptionLength"),
                    ExpressionUtils.as(availableForSale, "isAvailableForSale"),
                    qContent.status.stringValue().as("status"),
                    ExpressionUtils.as(isDeletableExpr, "isDeletable"),
                    qContent.isSearchExposed.as("isSearchExposed")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .leftJoin(qContent.category, qCategory)
            .where(whereClause);

    // 8) 정렬 적용
    if (pageable.getSort().isUnsorted()) {
      query.orderBy(qContent.createdAt.desc());
    } else {
      PathBuilder<Content> path = new PathBuilder<>(Content.class, qContent.getMetadata());
      for (Sort.Order o : pageable.getSort()) {
        Order dir = o.isAscending() ? Order.ASC : Order.DESC;
        query.orderBy(
            new OrderSpecifier<>(dir, path.getComparable(o.getProperty(), Comparable.class)));
      }
    }

    // 9) 페이징 & 결과
    List<FlatContentPreviewDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    long total =
        Optional.ofNullable(
                queryFactory.select(qContent.count()).from(qContent).where(whereClause).fetchOne())
            .orElse(0L);

    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Page<FlatContentOverviewDTO> findMyContentsBySellerId(Long sellerId, Pageable pageable) {
    QContent qContent = QContent.content;

    List<FlatContentOverviewDTO> contents =
        queryFactory
            .select(
                Projections.constructor(
                    FlatContentOverviewDTO.class,
                    qContent.id, // 콘텐츠 ID
                    qContent.title // 콘텐츠 제목
                    ))
            .from(qContent)
            .where(qContent.user.id.eq(sellerId), qContent.status.ne(ContentStatus.DELETED))
            .orderBy(qContent.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    // 3. 결과가 없는 경우 빈 리스트 처리
    if (contents == null || contents.isEmpty()) {
      contents = new ArrayList<>();
    }
    // 4. 전체 카운트 조회
    long total =
        Optional.ofNullable(
                queryFactory
                    .select(qContent.count())
                    .from(qContent)
                    .where(qContent.user.id.eq(sellerId), qContent.status.ne(ContentStatus.DELETED))
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(contents, pageable, total);
  }

  @Override
  public boolean isAvailableForSale(Long contentId) {
    QContent qContent = QContent.content;
    QDocumentOption qDocumentOption = QDocumentOption.documentOption;
    QCoachingOption qCoachingOption = QCoachingOption.coachingOption;

    // 1) 콘텐츠 필수 필드 검사
    BooleanExpression contentValid =
        qContent
            .thumbnailUrl
            .isNotNull()
            .and(qContent.status.eq(ContentStatus.DRAFT))
            .and(qContent.title.isNotNull())
            .and(qContent.makerIntro.isNotNull())
            .and(qContent.serviceProcess.isNotNull())
            .and(qContent.serviceTarget.isNotNull());

    // 2) DOCUMENT 타입일 때 옵션 검사
    BooleanExpression hasValidDocOpt =
        JPAExpressions.selectOne()
            .from(qDocumentOption)
            .where(
                qDocumentOption.content.id.eq(contentId),
                qDocumentOption.name.isNotNull(),
                qDocumentOption.description.isNotNull(),
                // 파일(URL+이름) 또는 링크 중 하나는 필수
                qDocumentOption
                    .documentFileUrl
                    .isNotNull()
                    .and(qDocumentOption.documentOriginalFileName.isNotNull())
                    .or(qDocumentOption.documentLinkUrl.isNotNull()))
            .exists();

    // 3) COACHING 타입일 때 옵션 검사
    BooleanExpression hasValidCoachingOpt =
        JPAExpressions.selectOne()
            .from(qCoachingOption)
            .where(
                qCoachingOption.content.id.eq(contentId),
                qCoachingOption.name.isNotNull(),
                qCoachingOption.description.isNotNull())
            .exists();

    // 4) ContentType 가져오기
    ContentType contentType =
        queryFactory
            .select(qContent.contentType)
            .from(qContent)
            .where(qContent.id.eq(contentId))
            .fetchOne();

    if (contentType == null) {
      return false;
    }

    // 5) 타입별 조건 검사
    BooleanExpression finalCondition;
    if (contentType == ContentType.DOCUMENT) {
      finalCondition = contentValid.and(hasValidDocOpt);
    } else if (contentType == ContentType.COACHING) {
      finalCondition = contentValid.and(hasValidCoachingOpt);
    } else {
      return false;
    }

    // 6) 최종 쿼리
    Boolean result =
        queryFactory
            .select(Expressions.constant(true))
            .from(qContent)
            .where(qContent.id.eq(contentId), finalCondition)
            .fetchFirst();

    return result != null;
  }

  @Override
  public long countByStatus(ContentStatus status) {
    QContent qContent = QContent.content;
    return Optional.ofNullable(
            queryFactory
                .select(qContent.count())
                .from(qContent)
                .where(qContent.status.eq(status))
                .fetchOne())
        .orElse(0L);
  }

  @Override
  public long countByStatusIn(List<ContentStatus> statuses) {
    QContent qContent = QContent.content;
    return Optional.ofNullable(
            queryFactory
                .select(qContent.count())
                .from(qContent)
                .where(qContent.status.in(statuses))
                .fetchOne())
        .orElse(0L);
  }

  @Override
  public long countByContentTypeAndStatus(String contentType, ContentStatus status) {
    QContent qContent = QContent.content;
    return Optional.ofNullable(
            queryFactory
                .select(qContent.count())
                .from(qContent)
                .where(
                    qContent
                        .contentType
                        .stringValue()
                        .eq(contentType)
                        .and(qContent.status.eq(status)))
                .fetchOne())
        .orElse(0L);
  }

  @Override
  public long countByContentTypeAndStatusIn(String contentType, List<ContentStatus> statuses) {
    QContent qContent = QContent.content;
    return Optional.ofNullable(
            queryFactory
                .select(qContent.count())
                .from(qContent)
                .where(
                    qContent
                        .contentType
                        .stringValue()
                        .eq(contentType)
                        .and(qContent.status.in(statuses)))
                .fetchOne())
        .orElse(0L);
  }
}
