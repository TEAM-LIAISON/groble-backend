package liaison.groble.persistence.settlement;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.settlement.entity.QSettlement;
import liaison.groble.domain.settlement.entity.QTaxInvoice;
import liaison.groble.domain.settlement.entity.TaxInvoice;
import liaison.groble.domain.settlement.repository.TaxInvoiceCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TaxInvoiceCustomRepositoryImpl implements TaxInvoiceCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  /**
   * 특정 사용자의 특정 연월 세금계산서 조회
   *
   * @param userId 사용자 ID
   * @param yearMonth 조회할 연월
   * @return 세금계산서 Optional
   */
  @Override
  public Optional<TaxInvoice> findByUserAndYearMonth(Long userId, YearMonth yearMonth) {
    QTaxInvoice qTaxInvoice = QTaxInvoice.taxInvoice;
    QSettlement qSettlement = QSettlement.settlement;

    LocalDate startDate = yearMonth.atDay(1);
    LocalDate endDate = yearMonth.atEndOfMonth();

    BooleanExpression conditions =
        qSettlement
            .settlementStartDate
            .eq(startDate)
            .and(qSettlement.settlementEndDate.eq(endDate));

    TaxInvoice result =
        jpaQueryFactory
            .selectFrom(qTaxInvoice)
            .leftJoin(qTaxInvoice.settlement, qSettlement)
            .where(
                qTaxInvoice.settlement.user.id.eq(userId),
                qTaxInvoice.invoiceType.eq(TaxInvoice.InvoiceType.MONTHLY),
                qTaxInvoice.status.eq(TaxInvoice.InvoiceStatus.ISSUED),
                conditions)
            .orderBy(qTaxInvoice.issuedDate.desc())
            .fetchFirst();

    return Optional.ofNullable(result);
  }
}
