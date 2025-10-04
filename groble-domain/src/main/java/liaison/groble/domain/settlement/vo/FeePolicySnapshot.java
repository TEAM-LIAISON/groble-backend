package liaison.groble.domain.settlement.vo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 수수료 정책 적용을 위해 필요한 수수료율 스냅샷.
 *
 * <p>각 필드는 null 이 될 수 없도록 생성 시 정규화된다.
 */
public record FeePolicySnapshot(
    BigDecimal platformFeeRateApplied,
    BigDecimal platformFeeRateDisplay,
    BigDecimal platformFeeRateBaseline,
    BigDecimal pgFeeRateApplied,
    BigDecimal pgFeeRateDisplay,
    BigDecimal pgFeeRateBaseline,
    BigDecimal vatRate) {

  public FeePolicySnapshot {
    platformFeeRateApplied = normalize(platformFeeRateApplied, "0.0150");
    platformFeeRateDisplay =
        normalize(platformFeeRateDisplay, platformFeeRateApplied.toPlainString());
    platformFeeRateBaseline =
        normalize(platformFeeRateBaseline, platformFeeRateApplied.toPlainString());
    pgFeeRateApplied = normalize(pgFeeRateApplied, "0.0170");
    pgFeeRateDisplay = normalize(pgFeeRateDisplay, pgFeeRateApplied.toPlainString());
    pgFeeRateBaseline = normalize(pgFeeRateBaseline, pgFeeRateApplied.toPlainString());
    vatRate = normalize(vatRate, "0.1000");
  }

  private static BigDecimal normalize(BigDecimal value, String fallback) {
    return Objects.requireNonNullElseGet(value, () -> new BigDecimal(fallback));
  }
}
