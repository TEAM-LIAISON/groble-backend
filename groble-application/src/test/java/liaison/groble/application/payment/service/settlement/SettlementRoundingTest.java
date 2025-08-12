package liaison.groble.application.payment.service.settlement;

import static liaison.groble.application.settlement.service.SettlementCalculationHelper.calculateFeeInWon;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** 테스트 케이스 - 원화 반올림 검증 */
@SpringBootTest(classes = liaison.groble.api.server.GrobleApiServerApplication.class)
@Transactional
public class SettlementRoundingTest {
  private static BigDecimal bd(String s) {
    return new BigDecimal(s);
  }

  @Test
  void 원화_반올림_테스트() {
    // Given
    BigDecimal salesAmount = new BigDecimal("100"); // 100원
    BigDecimal platformFeeRate = new BigDecimal("0.015"); // 1.5%
    BigDecimal pgFeeRate = new BigDecimal("0.017"); // 1.7%

    // When
    BigDecimal platformFee = calculateFeeInWon(salesAmount, platformFeeRate);
    BigDecimal pgFee = calculateFeeInWon(salesAmount, pgFeeRate);

    // Then
    assertThat(platformFee).isEqualTo(new BigDecimal("2")); // 1.5 → 2원
    assertThat(pgFee).isEqualTo(new BigDecimal("2")); // 1.7 → 2원
  }

  @Test
  void 다양한_금액_반올림_테스트() {
    // 333원 × 1.5% = 4.995 → 5원
    assertThat(calculateFeeInWon(bd("333"), bd("0.015"))).isEqualByComparingTo("5");

    // 1000원 × 1.7% = 17원
    assertThat(calculateFeeInWon(bd("1000"), bd("0.017"))).isEqualByComparingTo("17");

    // 99원 × 3.2% = 3.168 → 3원
    assertThat(calculateFeeInWon(bd("99"), bd("0.032"))).isEqualByComparingTo("3");
  }
}
