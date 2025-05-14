package liaison.groble.persistence.content;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
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
                    qUser.nickname.as("sellerName"),
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
                qUser.nickname.as("sellerName"),
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
                qUser.nickname.as("sellerName"),
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
                    qUser.nickname.as("sellerName"),
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
                    qUser.nickname.as("sellerName"),
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
                    qUser.nickname.as("sellerName"),
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
    BooleanExpression cond = q.contentType.eq(contentType).and(q.status.eq(ContentStatus.ACTIVE));

    List<FlatContentPreviewDTO> items =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    q.id.as("contentId"),
                    q.createdAt.as("createdAt"),
                    q.title.as("title"),
                    q.thumbnailUrl.as("thumbnailUrl"),
                    u.nickname.as("sellerName"),
                    q.lowestPrice.as("lowestPrice"),
                    q.status.stringValue().as("status")))
            .from(q)
            .leftJoin(q.user, u)
            .where(cond)
            .orderBy(q.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total = queryFactory.select(q.count()).from(q).where(cond).fetchOne();

    return new PageImpl<>(items, pageable, total != null ? total : 0L);
  }

  @Override
  public Page<FlatContentPreviewDTO> findContentsByCategoryAndType(
      Long categoryId, ContentType contentType, Pageable pageable) {

    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    // 1) 조건: 카테고리, 타입, ACTIVE
    BooleanExpression conditions =
        qContent
            .category
            .id
            .eq(categoryId)
            .and(qContent.contentType.eq(contentType))
            .and(qContent.status.eq(ContentStatus.ACTIVE));

    // 2) 데이터 조회 (offset/limit 적용, 최신순)
    List<FlatContentPreviewDTO> items =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.nickname.as("sellerName"),
                    qContent.lowestPrice.as("lowestPrice"),
                    qContent.status.stringValue().as("status")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .where(conditions)
            .orderBy(qContent.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    // 3) 전체 개수 조회
    Long total = queryFactory.select(qContent.count()).from(qContent).where(conditions).fetchOne();
    long totalCount = (total != null ? total : 0L);

    // 4) PageImpl 반환
    return new PageImpl<>(items, pageable, totalCount);
  }
}
