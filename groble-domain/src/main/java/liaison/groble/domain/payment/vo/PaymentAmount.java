package liaison.groble.domain.payment.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import jakarta.persistence.Embeddable;

/**
 * 결제 금액 Value Object
 *
 * <p>결제 금액에 대한 비즈니스 규칙과 검증 로직을 캡슐화합니다. Value Object 패턴을 통해 금액의 무결성과 불변성을 보장합니다.
 *
 * <p><strong>비즈니스 규칙:</strong>
 *
 * <ul>
 *   <li>금액은 0보다 커야 합니다
 *   <li>소수점 둘째 자리까지 지원
 *   <li>최대 금액 제한: 1,000만원
 *   <li>통화는 KRW 고정
 * </ul>
 */
@Embeddable
public class PaymentAmount {

  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000000.00"); // 1억
  private static final int SCALE = 0; // 소수점 둘째 자리

  private BigDecimal value;

  /** JPA용 protected 무인자 생성자 (반드시 필요) */
  protected PaymentAmount() {}

  /** 내부 사용용 생성자(팩토리에서만 호출) */
  private PaymentAmount(BigDecimal value) {
    this.value = value;
  }

  /**
   * 결제 금액을 생성합니다.
   *
   * @param amount 금액
   * @return 결제 금액 Value Object
   * @throws IllegalArgumentException 유효하지 않은 금액인 경우
   */
  public static PaymentAmount of(BigDecimal amount) {
    validateAmount(amount);
    BigDecimal scaledAmount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    return new PaymentAmount(scaledAmount);
  }

  /**
   * 결제 금액을 생성합니다.
   *
   * @param amount 금액 (문자열)
   * @return 결제 금액 Value Object
   * @throws IllegalArgumentException 유효하지 않은 금액인 경우
   */
  public static PaymentAmount of(String amount) {
    try {
      return of(new BigDecimal(amount));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("유효하지 않은 금액 형식입니다: " + amount, e);
    }
  }

  /**
   * 0원 금액을 생성합니다.
   *
   * @return 0원 결제 금액
   */
  public static PaymentAmount zero() {
    return new PaymentAmount(ZERO);
  }

  /**
   * 금액 값을 반환합니다.
   *
   * @return BigDecimal 금액
   */
  public BigDecimal getValue() {
    return value;
  }

  /**
   * 다른 금액과 더합니다.
   *
   * @param other 더할 금액
   * @return 합산된 새로운 결제 금액
   */
  public PaymentAmount add(PaymentAmount other) {
    return new PaymentAmount(this.value.add(other.value));
  }

  /**
   * 다른 금액을 뺍니다.
   *
   * @param other 뺄 금액
   * @return 차감된 새로운 결제 금액
   * @throws IllegalArgumentException 결과가 음수인 경우
   */
  public PaymentAmount subtract(PaymentAmount other) {
    BigDecimal result = this.value.subtract(other.value);
    if (result.compareTo(ZERO) < 0) {
      throw new IllegalArgumentException("결제 금액은 0보다 작을 수 없습니다");
    }
    return new PaymentAmount(result);
  }

  /**
   * 0원인지 확인합니다.
   *
   * @return 0원인 경우 true
   */
  public boolean isZero() {
    return value.compareTo(ZERO) == 0;
  }

  /**
   * 다른 금액보다 큰지 확인합니다.
   *
   * @param other 비교할 금액
   * @return 큰 경우 true
   */
  public boolean isGreaterThan(PaymentAmount other) {
    return value.compareTo(other.value) > 0;
  }

  /**
   * 다른 금액과 같은지 확인합니다.
   *
   * @param other 비교할 금액
   * @return 같은 경우 true
   */
  public boolean isEqualTo(PaymentAmount other) {
    return value.compareTo(other.value) == 0;
  }

  private static void validateAmount(BigDecimal amount) {
    if (amount == null) {
      throw new IllegalArgumentException("금액은 null일 수 없습니다");
    }
    if (amount.compareTo(ZERO) <= 0) {
      throw new IllegalArgumentException("금액은 0보다 커야 합니다: " + amount);
    }
    if (amount.compareTo(MAX_AMOUNT) > 0) {
      throw new IllegalArgumentException("금액이 최대 한도를 초과했습니다: " + amount);
    }
    if (amount.scale() > SCALE) {
      throw new IllegalArgumentException("금액은 소수점 " + SCALE + "자리까지만 지원됩니다: " + amount);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    PaymentAmount that = (PaymentAmount) obj;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value.toString() + " KRW";
  }
}
