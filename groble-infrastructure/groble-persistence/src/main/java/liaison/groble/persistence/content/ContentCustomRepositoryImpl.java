package liaison.groble.persistence.content;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentCustomRepositoryImpl implements ContentCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<FlatContentPreviewDTO> findFlatContentById(Long contentId) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

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
                    qContent.lowestPrice.as("lowestPrice"),
                    qContent.status.stringValue().as("status")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .where(qContent.id.eq(contentId))
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<FlatContentPreviewDTO> findFlatContentsByUserId(Long userId) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    return queryFactory
        .select(
            Projections.fields(
                FlatContentPreviewDTO.class,
                qContent.id.as("contentId"),
                qContent.createdAt.as("createdAt"),
                qContent.title.as("title"),
                qContent.thumbnailUrl.as("thumbnailUrl"),
                qUser.userProfile.nickname.as("sellerName"),
                qContent.status.stringValue().as("status")))
        .from(qContent)
        .where(qContent.user.id.eq(userId))
        .fetch();
  }

  @Override
  public List<FlatContentPreviewDTO> findHomeContents(ContentType contentType) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    BooleanExpression conditions =
        qContent.contentType.eq(contentType).and(qContent.status.eq(ContentStatus.ACTIVE));

    return queryFactory
        .select(
            Projections.fields(
                FlatContentPreviewDTO.class,
                qContent.id.as("contentId"),
                qContent.createdAt.as("createdAt"),
                qContent.title.as("title"),
                qContent.thumbnailUrl.as("thumbnailUrl"),
                qUser.userProfile.nickname.as("sellerName"),
                qContent.lowestPrice.as("lowestPrice"),
                qContent.status.stringValue().as("status")))
        .from(qContent)
        .leftJoin(qContent.user, qUser)
        .where(conditions)
        .orderBy(qContent.createdAt.desc()) // 최신순 정렬
        .limit(12) // 최대 12개로 제한
        .fetch();
  }

  @Override
  public CursorResponse<FlatContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, ContentStatus status, ContentType contentType) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

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
                    qContent.status.stringValue().as("status")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
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
      Long userId,
      Long lastContentId,
      int size,
      List<ContentStatus> statusList,
      ContentType contentType) {

    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    // 기본 조건 설정
    BooleanExpression conditions =
        qContent.user.id.eq(userId).and(qContent.contentType.eq(contentType));

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
                    qContent.lowestPrice.as("lowestPrice"),
                    qContent.status.stringValue().as("status")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
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
    String filterValue = null;
    if (statusList != null && !statusList.isEmpty()) {
      if (statusList.size() == 2
          && statusList.contains(ContentStatus.VALIDATED)
          && statusList.contains(ContentStatus.REJECTED)) {
        filterValue = "APPROVED"; // VALIDATED와 REJECTED를 함께 조회할 경우 "APPROVED"로 표시
      } else {
        filterValue = statusList.stream().map(ContentStatus::name).collect(Collectors.joining(","));
      }
    }

    CursorResponse.MetaData meta =
        CursorResponse.MetaData.builder().filter(filterValue).cursorType("id").build();

    return CursorResponse.of(items, nextCursor, hasNext, 0, meta);
  }

  @Override
  public CursorResponse<FlatContentPreviewDTO> findHomeContentsWithCursor(
      Long lastContentId, int size, ContentType contentType) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

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
                    qContent.status.stringValue().as("status")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
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
  public int countMySellingContents(
      Long userId, List<ContentStatus> statusList, ContentType contentType) {
    QContent qContent = QContent.content;

    // 기본 조건 설정
    BooleanExpression conditions =
        qContent.user.id.eq(userId).and(qContent.contentType.eq(contentType));

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
                    q.status.stringValue().as("status")))
            .from(q)
            .leftJoin(q.user, u)
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
  public Page<FlatContentPreviewDTO> findContentsByCategoriesAndType(
      List<String> categoryCodes, ContentType type, Pageable pageable) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
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
                    qContent.lowestPrice.as("lowestPrice"),
                    qContent.status.stringValue().as("status")))
            .from(qContent)
            .leftJoin(qContent.user, qUser) // 사용자 정보는 반드시 필요하니까 남겨두고
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
}
