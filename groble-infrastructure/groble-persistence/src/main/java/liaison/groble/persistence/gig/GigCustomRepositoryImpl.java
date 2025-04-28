package liaison.groble.persistence.gig;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.gig.dto.FlatPreviewGigDTO;
import liaison.groble.domain.gig.entity.QCategory;
import liaison.groble.domain.gig.entity.QGig;
import liaison.groble.domain.gig.enums.GigStatus;
import liaison.groble.domain.gig.repository.GigCustomRepository;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GigCustomRepositoryImpl implements GigCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<FlatPreviewGigDTO> findFlatGigById(Long gigId) {
    QGig qGig = QGig.gig;
    QUser qUser = QUser.user;

    FlatPreviewGigDTO result =
        queryFactory
            .select(
                Projections.fields(
                    FlatPreviewGigDTO.class,
                    qGig.id.as("gigId"),
                    qGig.createdAt.as("createdAt"),
                    qGig.title.as("title"),
                    qGig.thumbnailUrl.as("thumbnailUrl"),
                    qUser.nickname.as("sellerName"),
                    qGig.status.stringValue().as("status")))
            .from(qGig)
            .leftJoin(qGig.user, qUser)
            .where(qGig.id.eq(gigId))
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<FlatPreviewGigDTO> findFlatGigsByUserId(Long userId) {
    QGig qGig = QGig.gig;
    QUser qUser = QUser.user;

    return queryFactory
        .select(
            Projections.fields(
                FlatPreviewGigDTO.class,
                qGig.id.as("gigId"),
                qGig.createdAt.as("createdAt"),
                qGig.title.as("title"),
                qGig.thumbnailUrl.as("thumbnailUrl"),
                qUser.nickname.as("sellerName"),
                qGig.status.stringValue().as("status")))
        .from(qGig)
        .where(qGig.user.id.eq(userId))
        .fetch();
  }

  @Override
  public CursorResponse<FlatPreviewGigDTO> findMyCoachingGigsWithCursor(
      Long userId, Long lastGigId, int size, List<Long> categoryIds, GigStatus status) {

    QGig qGig = QGig.gig;
    QUser qUser = QUser.user;

    // 기본 조건 설정
    BooleanExpression conditions = qGig.user.id.eq(userId);

    // 커서 조건 추가
    if (lastGigId != null) {
      conditions = conditions.and(qGig.id.lt(lastGigId));
    }

    // 상태 필터 추가
    if (status != null) {
      conditions = conditions.and(qGig.status.eq(status));
    }

    // 조회할 개수 + 1 (다음 페이지 존재 여부 확인용)
    int fetchSize = size + 1;

    // 쿼리 실행
    List<FlatPreviewGigDTO> results =
        queryFactory
            .select(
                Projections.fields(
                    FlatPreviewGigDTO.class,
                    qGig.id.as("gigId"),
                    qGig.createdAt.as("createdAt"),
                    qGig.title.as("title"),
                    qGig.thumbnailUrl.as("thumbnailUrl"),
                    qUser.nickname.as("sellerName"),
                    qGig.status.stringValue().as("status")))
            .from(qGig)
            .leftJoin(qGig.user, qUser)
            .where(conditions)
            .orderBy(qGig.id.desc())
            .limit(fetchSize)
            .fetch();

    // 다음 페이지 여부 확인
    boolean hasNext = results.size() > size;

    // 실제 반환할 리스트 조정
    List<FlatPreviewGigDTO> items = hasNext ? results.subList(0, size) : results;

    // 다음 커서 계산
    String nextCursor = null;
    if (hasNext && !items.isEmpty()) {
      nextCursor = String.valueOf(items.get(items.size() - 1).getGigId());
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
  public int countMyCoachingGigs(Long userId, List<Long> categoryIds, GigStatus status) {
    QGig qGig = QGig.gig;
    QCategory qCategory = QCategory.category;

    // 기본 조건 설정
    BooleanExpression conditions = qGig.user.id.eq(userId).and(qCategory.id.in(categoryIds));

    // 상태 필터 추가
    if (status != null) {
      conditions = conditions.and(qGig.status.eq(status));
    }

    return Math.toIntExact(
        queryFactory
            .select(qGig.count())
            .from(qGig)
            .join(qGig.category, qCategory)
            .where(conditions)
            .fetchOne());
  }
}
