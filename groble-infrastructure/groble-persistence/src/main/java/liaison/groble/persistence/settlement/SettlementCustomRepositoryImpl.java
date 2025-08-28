package liaison.groble.persistence.settlement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.settlement.dto.FlatPerTransactionSettlement;
import liaison.groble.domain.settlement.dto.FlatSettlementsDTO;
import liaison.groble.domain.settlement.entity.QSettlement;
import liaison.groble.domain.settlement.entity.QSettlementItem;
import liaison.groble.domain.settlement.enums.SettlementType;
import liaison.groble.domain.settlement.repository.SettlementCustomRepository;
import liaison.groble.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SettlementCustomRepositoryImpl implements SettlementCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<FlatSettlementsDTO> findSettlementsByUserId(Long userId, Pageable pageable) {
    QSettlement qSettlement = QSettlement.settlement;
    QUser user = QUser.user;

    BooleanExpression cond = qSettlement.user.id.eq(userId);

    // settlementType에 따른 정렬 우선순위 설정 (COACHING = 0, DOCUMENT = 1)
    NumberExpression<Integer> typeOrder =
        new CaseBuilder()
            .when(qSettlement.settlementType.eq(SettlementType.COACHING))
            .then(0)
            .when(qSettlement.settlementType.eq(SettlementType.DOCUMENT))
            .then(1)
            .otherwise(2);
    // 메인 쿼리
    JPAQuery<FlatSettlementsDTO> query =
        jpaQueryFactory
            .select(
                Projections.fields(
                    FlatSettlementsDTO.class,
                    qSettlement.id.as("settlementId"),
                    qSettlement.settlementStartDate.as("settlementStartDate"),
                    qSettlement.settlementEndDate.as("settlementEndDate"),
                    qSettlement.scheduledSettlementDate.as("scheduledSettlementDate"),
                    qSettlement.settlementType.stringValue().as("contentType"),
                    qSettlement.settlementAmount.as("settlementAmount"),
                    qSettlement.status.stringValue().as("settlementStatus")))
            .from(qSettlement)
            .leftJoin(qSettlement.user, user)
            .where(cond)
            .orderBy(
                qSettlement.scheduledSettlementDate.desc(), // 정산 예정일 기준 정렬
                typeOrder.asc(), // COACHING이 먼저 (0 < 1)
                qSettlement.id.desc() // tie-breaker
                );

    List<FlatSettlementsDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    long total =
        Optional.ofNullable(
                jpaQueryFactory
                    .select(qSettlement.count())
                    .from(qSettlement)
                    .leftJoin(qSettlement.user, user)
                    .where(cond)
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Page<FlatPerTransactionSettlement> findPerTransactionSettlementsByIdAndUserId(
      Long userId, Long settlementId, Pageable pageable) {

    QSettlementItem qSettlementItem = QSettlementItem.settlementItem;
    QSettlement qSettlement = QSettlement.settlement;
    QUser qUser = QUser.user;

    // [기간] purchasedAt ∈ [periodStart 00:00, periodEnd+1 00:00)
    BooleanExpression cond =
        qSettlementItem.settlement.user.id.eq(userId).and(qSettlement.id.eq(settlementId));

    // 메인 조회
    JPAQuery<FlatPerTransactionSettlement> query =
        jpaQueryFactory
            .select(
                Projections.fields(
                    FlatPerTransactionSettlement.class,
                    qSettlementItem.contentTitle.as("contentTitle"),
                    qSettlementItem.settlementAmount.as("settlementAmount"),
                    qSettlementItem.purchasedAt.as("purchasedAt")))
            .from(qSettlementItem)
            .leftJoin(qSettlementItem.settlement, qSettlement)
            .leftJoin(qSettlement.user, qUser)
            .where(cond)
            .orderBy(qSettlementItem.purchasedAt.desc());

    // 페이징
    List<FlatPerTransactionSettlement> content =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 카운트
    Long total =
        Optional.ofNullable(
                jpaQueryFactory
                    .select(qSettlementItem.count())
                    .from(qSettlementItem)
                    .join(qSettlementItem.settlement, qSettlement)
                    .where(cond)
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(content, pageable, total);
  }
}
