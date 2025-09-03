package liaison.groble.external.adapter.payment;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PaypleService 사용법 예시
 *
 * <p>개선된 API 사용법을 보여주는 예시 클래스입니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaypleServiceUsageExample {

  private final PaypleService paypleService;
  private final PaypleCodeGenerator codeGenerator;

  /** 기존 방식: 일반 결제 인증 (호환성 유지) */
  public JSONObject authenticateForPayment() {
    Map<String, String> params = new HashMap<>();
    params.put("PCD_PAY_WORK", "LINKREG"); // 또는 "AUTH", "PAY" 등

    return paypleService.payAuth(params);
  }

  /** 새로운 방식: 결제 취소 전용 인증 PCD_PAY_WORK 대신 PCD_PAYCANCEL_FLAG 사용 */
  public JSONObject authenticateForCancel() {
    log.info("결제 취소 인증 시작");

    // 필요한 파라미터: cst_id, custKey, PCD_PAYCANCEL_FLAG
    JSONObject result = paypleService.payAuthForCancel();

    if ("success".equals(getStringValue(result, "result"))) {
      log.info("결제 취소 인증 성공 - AuthKey: {}", getStringValue(result, "AuthKey"));
    } else {
      log.error("결제 취소 인증 실패 - {}", getStringValue(result, "result_msg"));
    }

    return result;
  }

  /** 새로운 방식: 정산지급대행 전용 인증 영문+숫자 10자리 code 필요 */
  public JSONObject authenticateForSettlement() {
    log.info("정산지급대행 인증 시작");

    // 랜덤 코드 생성 (실제로는 비즈니스 로직에 따라 결정)
    String code = codeGenerator.generateSettlementCode();

    // 필요한 파라미터: cst_id, custKey, code
    JSONObject result = paypleService.payAuthForSettlement(code);

    if ("success".equals(getStringValue(result, "result"))) {
      log.info("정산지급대행 인증 성공 - AuthKey: {}", getStringValue(result, "AuthKey"));
    } else {
      log.error("정산지급대행 인증 실패 - {}", getStringValue(result, "result_msg"));
    }

    return result;
  }

  /** 타임스탬프 기반 코드로 정산지급대행 인증 */
  public JSONObject authenticateForSettlementWithTimestamp() {
    log.info("타임스탬프 기반 정산지급대행 인증 시작");

    String timestampCode = codeGenerator.generateTimestampBasedCode();

    JSONObject result = paypleService.payAuthForSettlement(timestampCode);

    if ("success".equals(getStringValue(result, "result"))) {
      log.info("타임스탬프 기반 정산지급대행 인증 성공");
    } else {
      log.error("타임스탬프 기반 정산지급대행 인증 실패");
    }

    return result;
  }

  /** JSONObject에서 String 값을 안전하게 가져오는 헬퍼 메서드 */
  private String getStringValue(JSONObject json, String key) {
    Object value = json.get(key);
    return value != null ? String.valueOf(value) : "";
  }
}
