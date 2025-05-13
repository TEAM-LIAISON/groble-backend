package liaison.groble.persistence.scrap;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatScrapContentPreviewDTO;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.scrap.entity.QContentScrap;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ContentScrapCustomRepositoryImpl implements ContentScrapCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public CursorResponse<FlatScrapContentPreviewDTO> getMyScrapContentsWithCursor(
      Long userId, Long lastContentId, int size, ContentType contentType) {
    QContentScrap qContentScrap = QContentScrap.contentScrap;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;

    BooleanExpression conditions =
        qContentScrap.user.id.eq(userId).and(qContent.contentType.eq(contentType));

    // 커서 조건 추가
    if (lastContentId != null) {
      conditions = conditions.and(qContent.id.lt(lastContentId));
    }

    // 조회할 개수 + 1 (다음 페이지 존재 여부 확인용)
    int fetchSize = size + 1;

    // 쿼리 실행
    List<FlatScrapContentPreviewDTO> results =
        jpaQueryFactory
            .select(
                Projections.fields(
                    FlatScrapContentPreviewDTO.class,
                    qContent.id.as("contentId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qUser.nickname.as("sellerName"),
                    qContentScrap.isNotNull().as("isContentScrap")))
            .from(qContentScrap)
            .join(qContentScrap.content, qContent)
            .join(qContent.user, qUser)
            .where(conditions)
            .orderBy(qContent.id.desc())
            .limit(fetchSize)
            .fetch();

    // 다음 페이지 여부 확인
    boolean hasNext = results.size() > size;

    // 실제 반환할 리스트 조정
    List<FlatScrapContentPreviewDTO> items = hasNext ? results.subList(0, size) : results;

    // 다음 커서 계산
    String nextCursor = null;
    if (hasNext && !items.isEmpty()) {
      nextCursor = String.valueOf(items.get(items.size() - 1).getContentId());
    }

    // 메타데이터
    CursorResponse.MetaData meta =
        CursorResponse.MetaData.builder()
            .filter(contentType != null ? contentType.name() : null)
            .cursorType("id")
            .build();

    return CursorResponse.of(items, nextCursor, hasNext, 0, meta);
  }

  @Override
  public int countMyScrapContents(Long userId, ContentType contentType) {
    QContentScrap qContentScrap = QContentScrap.contentScrap;
    QContent qContent = QContent.content;

    // 기본 조건 설정
    BooleanExpression conditions =
        qContentScrap.user.id.eq(userId).and(qContent.contentType.eq(contentType));

    // join을 적용한 카운트 쿼리
    Long count =
        jpaQueryFactory
            .select(qContentScrap.count())
            .from(qContentScrap)
            .join(qContentScrap.content, qContent)
            .where(conditions)
            .fetchOne();

    // null 체크 후 반환 (결과가 null이면 0 반환)
    return count != null ? count.intValue() : 0;
  }
}
