package liaison.groble.external.adapter.payment;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Payple 정산지급대행에 필요한 code 생성기
 *
 * <p>영문+숫자 조합 10자리 코드를 생성합니다.
 */
@Component
@Slf4j
public class PaypleCodeGenerator {

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int CODE_LENGTH = 10;
  private final SecureRandom random = new SecureRandom();

  /**
   * 정산지급대행용 랜덤 코드 생성
   *
   * @return 영문+숫자 조합 10자리 코드
   */
  public String generateSettlementCode() {
    StringBuilder code = new StringBuilder(CODE_LENGTH);

    for (int i = 0; i < CODE_LENGTH; i++) {
      int randomIndex = random.nextInt(CHARACTERS.length());
      code.append(CHARACTERS.charAt(randomIndex));
    }

    String generatedCode = code.toString();
    log.debug("정산지급대행 코드 생성: {}", generatedCode);

    return generatedCode;
  }

  /**
   * 타임스탬프 기반 코드 생성 (더 예측 가능하지만 고유성 보장)
   *
   * @return 타임스탬프 기반 10자리 코드
   */
  public String generateTimestampBasedCode() {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));

    log.debug("타임스탬프 기반 코드 생성: {}", timestamp);

    return timestamp;
  }

  /**
   * 코드 검증
   *
   * @param code 검증할 코드
   * @return 유효성 여부
   */
  public boolean isValidCode(String code) {
    if (code == null || code.length() != CODE_LENGTH) {
      return false;
    }
    return code.matches("^[A-Z0-9]{10}$");
  }
}
