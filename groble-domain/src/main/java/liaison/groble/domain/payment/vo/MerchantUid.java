package liaison.groble.domain.payment.vo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 주문번호 Value Object
 *
 * <p>결제 시스템에서 사용되는 고유한 주문번호를 관리합니다. Value Object 패턴을 통해 주문번호의 형식과 유일성을 보장합니다.
 *
 * <p><strong>주문번호 형식:</strong>
 *
 * <ul>
 *   <li>ORDER-YYYYMMDD-HHMMSS-{UUID}
 *   <li>예: ORDER-20231201-143022-ABC123
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MerchantUid {

  private static final String PREFIX = "ORDER-";
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
  private static final Pattern VALID_PATTERN = Pattern.compile("^ORDER-\\d{8}-\\d{6}-[A-Z0-9]{6}$");

  private final String value;

  /**
   * 새로운 주문번호를 생성합니다.
   *
   * @return 새로운 주문번호
   */
  public static MerchantUid generate() {
    String timestamp = LocalDateTime.now().format(DATE_FORMAT);
    String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

    String merchantUid = PREFIX + timestamp + "-" + uniqueId;
    return new MerchantUid(merchantUid);
  }

  /**
   * 기존 주문번호로부터 Value Object를 생성합니다.
   *
   * @param merchantUid 주문번호 문자열
   * @return 주문번호 Value Object
   * @throws IllegalArgumentException 유효하지 않은 주문번호 형식인 경우
   */
  public static MerchantUid of(String merchantUid) {
    validateMerchantUid(merchantUid);
    return new MerchantUid(merchantUid);
  }

  /**
   * 주문번호 값을 반환합니다.
   *
   * @return 주문번호 문자열
   */
  public String getValue() {
    return value;
  }

  /**
   * 주문번호에서 날짜 부분을 추출합니다.
   *
   * @return 주문 생성 날짜 (YYYYMMDD 형식)
   */
  public String getOrderDate() {
    if (!isValid()) {
      throw new IllegalStateException("유효하지 않은 주문번호입니다: " + value);
    }
    return value.substring(6, 14); // ORDER- 이후 8자리
  }

  /**
   * 주문번호에서 시간 부분을 추출합니다.
   *
   * @return 주문 생성 시간 (HHMMSS 형식)
   */
  public String getOrderTime() {
    if (!isValid()) {
      throw new IllegalStateException("유효하지 않은 주문번호입니다: " + value);
    }
    return value.substring(15, 21); // 날짜 뒤 - 이후 6자리
  }

  /**
   * 주문번호가 유효한 형식인지 확인합니다.
   *
   * @return 유효한 경우 true
   */
  public boolean isValid() {
    return VALID_PATTERN.matcher(value).matches();
  }

  private static void validateMerchantUid(String merchantUid) {
    if (merchantUid == null || merchantUid.trim().isEmpty()) {
      throw new IllegalArgumentException("주문번호는 비어있을 수 없습니다");
    }

    if (!VALID_PATTERN.matcher(merchantUid).matches()) {
      throw new IllegalArgumentException("유효하지 않은 주문번호 형식입니다: " + merchantUid);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    MerchantUid that = (MerchantUid) obj;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
