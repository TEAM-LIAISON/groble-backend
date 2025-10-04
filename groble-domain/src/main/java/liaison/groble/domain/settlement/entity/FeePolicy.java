package liaison.groble.domain.settlement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.settlement.enums.FeePolicyScope;
import liaison.groble.domain.settlement.vo.FeePolicySnapshot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "fee_policies",
    indexes = {
      @Index(
          name = "idx_fee_policy_scope_effective",
          columnList = "scope_type, scope_reference, effective_from, effective_to")
    })
public class FeePolicy extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("수수료 정책 ID")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope_type", nullable = false, length = 20)
  @Comment("수수료 정책 적용 범위 타입")
  private FeePolicyScope scopeType;

  @Column(name = "scope_reference")
  @Comment("범위 참조 ID (예: SELLER 범위의 경우 판매자 ID)")
  private Long scopeReference;

  @Column(name = "effective_from", nullable = false)
  @Comment("정책 적용 시작 일시")
  private LocalDateTime effectiveFrom;

  @Column(name = "effective_to")
  @Comment("정책 적용 종료 일시")
  private LocalDateTime effectiveTo;

  @Column(name = "platform_fee_rate_applied", nullable = false, precision = 5, scale = 4)
  @Comment("실제 정산에 적용되는 플랫폼 수수료율")
  private BigDecimal platformFeeRateApplied;

  @Column(name = "platform_fee_rate_baseline", nullable = false, precision = 5, scale = 4)
  @Comment("기준 플랫폼 수수료율")
  private BigDecimal platformFeeRateBaseline;

  @Column(name = "platform_fee_rate_display", nullable = false, precision = 5, scale = 4)
  @Comment("표시용 플랫폼 수수료율")
  private BigDecimal platformFeeRateDisplay;

  @Column(name = "pg_fee_rate_applied", nullable = false, precision = 5, scale = 4)
  @Comment("실제 정산에 적용되는 PG 수수료율")
  private BigDecimal pgFeeRateApplied;

  @Column(name = "pg_fee_rate_baseline", nullable = false, precision = 5, scale = 4)
  @Comment("기준 PG 수수료율")
  private BigDecimal pgFeeRateBaseline;

  @Column(name = "pg_fee_rate_display", nullable = false, precision = 5, scale = 4)
  @Comment("표시용 PG 수수료율")
  private BigDecimal pgFeeRateDisplay;

  @Column(name = "vat_rate", nullable = false, precision = 5, scale = 4)
  @Comment("부가가치세율")
  private BigDecimal vatRate;

  public FeePolicySnapshot toSnapshot() {
    return new FeePolicySnapshot(
        platformFeeRateApplied,
        platformFeeRateDisplay,
        platformFeeRateBaseline,
        pgFeeRateApplied,
        pgFeeRateDisplay,
        pgFeeRateBaseline,
        vatRate);
  }

  public boolean isEffectiveAt(LocalDateTime target) {
    if (target.isBefore(effectiveFrom)) {
      return false;
    }
    return effectiveTo == null || target.isBefore(effectiveTo);
  }

  public boolean matchesScope(FeePolicyScope scopeType, Long scopeReference) {
    if (this.scopeType != scopeType) {
      return false;
    }
    if (scopeReference == null) {
      return this.scopeReference == null;
    }
    return scopeReference.equals(this.scopeReference);
  }
}
