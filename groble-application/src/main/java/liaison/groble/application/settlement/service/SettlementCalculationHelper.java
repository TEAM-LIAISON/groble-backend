package liaison.groble.application.settlement.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SettlementCalculationHelper {

  /**
   * 원화 수수료 계산 (반올림)
   *
   * @param amount 금액
   * @param rate 수수료율
   * @return 반올림된 수수료 (원 단위)
   */
  public static BigDecimal calculateFeeInWon(BigDecimal amount, BigDecimal rate) {
    if (amount == null || rate == null) {
      return BigDecimal.ZERO;
    }

    // 수수료 계산 후 원 단위로 반올림
    BigDecimal fee = amount.multiply(rate);
    return fee.setScale(0, RoundingMode.HALF_UP);
  }

  /**
   * 정산 금액 계산 (원화)
   *
   * @param salesAmount 판매 금액
   * @param platformFeeRate 플랫폼 수수료율
   * @param pgFeeRate PG 수수료율
   * @return 정산 계산 결과
   */
  public static SettlementCalculation calculate(
      BigDecimal salesAmount, BigDecimal platformFeeRate, BigDecimal pgFeeRate) {

    // 각 수수료를 개별적으로 반올림
    BigDecimal platformFee = calculateFeeInWon(salesAmount, platformFeeRate);
    BigDecimal pgFee = calculateFeeInWon(salesAmount, pgFeeRate);
    BigDecimal totalFee = platformFee.add(pgFee);
    BigDecimal settlementAmount = salesAmount.subtract(totalFee);

    log.debug(
        "정산 계산: 판매액={}, 플랫폼수수료={}, PG수수료={}, 정산액={}",
        salesAmount,
        platformFee,
        pgFee,
        settlementAmount);

    return SettlementCalculation.builder()
        .salesAmount(salesAmount)
        .platformFee(platformFee)
        .pgFee(pgFee)
        .totalFee(totalFee)
        .settlementAmount(settlementAmount)
        .build();
  }

  @Getter
  @Builder
  public static class SettlementCalculation {
    private BigDecimal salesAmount;
    private BigDecimal platformFee;
    private BigDecimal pgFee;
    private BigDecimal totalFee;
    private BigDecimal settlementAmount;
  }
}
