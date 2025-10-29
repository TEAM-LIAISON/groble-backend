package liaison.groble.application.payment.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.StringUtils;

import liaison.groble.application.payment.dto.PaypleApprovalResult;

/**
 * Payple 승인/환불 에러 코드를 사용자 친화적인 안내 문구로 변환한다.
 *
 * <p>Payple에서 내려주는 메시지는 "[PCCFD001] 1062 - ..."처럼 기술적인 정보를 포함하므로, 고객에게 즉시 이해 가능한 문장으로 매핑한다. 유효한 매핑이
 * 없을 경우에는 원본 메시지를 그대로 반환한다.
 */
public final class PaypleErrorMessageResolver {

  private static final Map<String, String> APPROVAL_DETAIL_MESSAGES = new HashMap<>();

  static {
    APPROVAL_DETAIL_MESSAGES.put("1062", "할부거래 사용불가 카드입니다. 다른 카드를 이용해주세요.");
  }

  private PaypleErrorMessageResolver() {}

  /**
   * Payple 승인 실패 응답에서 사용자 안내 문구를 도출한다.
   *
   * @param approvalResult Payple 승인 결과 DTO
   * @return 사용자에게 표시할 문구
   */
  public static String resolveApprovalFailureMessage(PaypleApprovalResult approvalResult) {
    if (approvalResult == null) {
      return defaultFailureMessage(null);
    }

    String userMessage =
        extractDetailCode(approvalResult.getErrorMessage())
            .map(APPROVAL_DETAIL_MESSAGES::get)
            .orElse(null);

    if (StringUtils.hasText(userMessage)) {
      return userMessage;
    }

    // 상세 코드 매핑이 없으면 Payple이 제공한 메시지를 그대로 사용한다.
    return defaultFailureMessage(approvalResult.getErrorMessage());
  }

  private static String defaultFailureMessage(String rawMessage) {
    if (StringUtils.hasText(rawMessage)) {
      return rawMessage;
    }
    return "결제 승인이 완료되지 않았습니다. 다른 결제 수단으로 다시 시도해주세요.";
  }

  private static Optional<String> extractDetailCode(String message) {
    if (!StringUtils.hasText(message)) {
      return Optional.empty();
    }

    // Payple 기본 형식: "[PCCFD001] 1062 - 할부거래 사용불가 카드"
    int start = message.indexOf(']');
    if (start >= 0 && start + 2 < message.length()) {
      int dashIndex = message.indexOf('-', start + 1);

      if (dashIndex > start) {
        String candidate = message.substring(start + 1, dashIndex).trim();
        if (candidate.matches("\\d{3,4}")) {
          return Optional.of(candidate);
        }
      }
    }

    // 일반적인 숫자 코드가 포함된 경우 마지막 4자리 숫자를 추출
    String digitsOnly = message.replaceAll("[^0-9]", " ").trim();
    if (StringUtils.hasText(digitsOnly)) {
      String[] tokens = digitsOnly.split("\\s+");
      for (int i = tokens.length - 1; i >= 0; i--) {
        if (tokens[i].matches("\\d{3,4}")) {
          return Optional.of(tokens[i]);
        }
      }
    }

    return Optional.empty();
  }
}
