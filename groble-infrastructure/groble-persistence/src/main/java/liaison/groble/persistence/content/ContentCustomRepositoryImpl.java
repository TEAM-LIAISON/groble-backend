package liaison.groble.persistence.content;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatPreviewContentDTO;
import liaison.groble.domain.content.entity.QCategory;
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
  public Optional<FlatPreviewContentDTO> findFlatContentById(Long contentId) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    FlatPreviewContentDTO result =
        queryFactory
            .select(
                Projections.fields(
                    FlatPreviewContentDTO.class,
                    qContent.id.as("contentId"),
                    qContent.createdAt.as("createdAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.nickname.as("sellerName"),
                    qContent.status.stringValue().as("status")))
            .from(qContent)
            .leftJoin(qContent.user, qUser)
            .where(qContent.id.eq(contentId))
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<FlatPreviewContentDTO> findFlatContentsByUserId(Long userId) {
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    return queryFactory
        .select(
            Projections.fields(
                FlatPreviewContentDTO.class,
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
  public CursorResponse<FlatPreviewContentDTO> findMyCoachingContentsWithCursor(
      Long userId, Long lastContentId, int size, List<Long> categoryIds, ContentStatus status) {

    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    // 기본 조건 설정
    BooleanExpression conditions =
        qContent.user.id.eq(userId).and(qContent.contentType.eq(ContentType.COACHING));

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
    List<FlatPreviewContentDTO> results =
        queryFactory
            .select(
                Projections.fields(
                    FlatPreviewContentDTO.class,
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
    List<FlatPreviewContentDTO> items = hasNext ? results.subList(0, size) : results;

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
  public int countMyCoachingContents(Long userId, List<Long> categoryIds, ContentStatus status) {
    QContent qContent = QContent.content;
    QCategory qCategory = QCategory.category;

    // 기본 조건 설정
    BooleanExpression conditions =
        qContent
            .user
            .id
            .eq(userId)
            .and(qCategory.id.in(categoryIds))
            .and(qContent.contentType.eq(ContentType.COACHING));

    // 상태 필터 추가
    if (status != null) {
      conditions = conditions.and(qContent.status.eq(status));
    }

    // 결과가 null일 경우를 대비한 안전한 처리
    Long count = queryFactory.select(qContent.count()).from(qContent).where(conditions).fetchOne();

    // null 체크 후 반환 (결과가 null이면 0 반환)
    return count != null ? count.intValue() : 0;
  }
}
