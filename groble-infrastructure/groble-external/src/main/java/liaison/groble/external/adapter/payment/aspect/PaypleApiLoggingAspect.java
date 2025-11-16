package liaison.groble.external.adapter.payment.aspect;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Payple API 호출에 대한 상세 로깅을 담당하는 Aspect
 *
 * <p>비즈니스 로직을 수정하지 않고 요청/응답 데이터를 예쁘게 로깅합니다. 민감한 데이터는 자동으로 마스킹되어 보안을 보장합니다.
 */
@Aspect
@Component
@Slf4j
public class PaypleApiLoggingAspect {

  /** PaypleService의 모든 메서드 호출을 가로채서 간소한 로깅 */
  @Around("execution(* liaison.groble.external.adapter.payment.PaypleService.*(..))")
  public Object logPaypleApiCalls(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    String apiName = getApiDisplayName(methodName);

    long startTime = System.currentTimeMillis();
    try {
      Object result = joinPoint.proceed();
      long duration = System.currentTimeMillis() - startTime;

      // 간소한 성공 로깅
      logSimpleSuccess(apiName, result, duration);
      return result;

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;

      // 간소한 실패 로깅
      logSimpleError(apiName, e, duration);
      throw e;
    }
  }

  /** API 이름을 사용자 친화적으로 변환 */
  private String getApiDisplayName(String methodName) {
    Map<String, String> nameMap = new HashMap<>();
    nameMap.put("payAuthForSettlement", "파트너인증");
    nameMap.put("payAccountVerification", "계좌인증");
    nameMap.put("payTransferRequest", "이체대기");
    nameMap.put("payTransferExecute", "이체실행");
    nameMap.put("payTransferCancel", "이체취소");
    nameMap.put("payAuth", "일반인증");
    nameMap.put("payConfirm", "결제승인");
    nameMap.put("payRefund", "결제취소");
    return nameMap.getOrDefault(methodName, methodName);
  }

  /** 간소한 성공 로깅 */
  private void logSimpleSuccess(String apiName, Object result, long duration) {
    try {
      if (result instanceof JSONObject) {
        JSONObject jsonResult = (JSONObject) result;
        // 새로운 API 응답 형식의 키들을 먼저 확인
        String resultCode = getStringValue(jsonResult, "result");
        String message = getStringValue(jsonResult, "message");

        // 기존 API 응답 형식 키들로 fallback
        if (resultCode.isEmpty()) {
          resultCode = getStringValue(jsonResult, "PCD_PAY_RST");
        }
        if (message.isEmpty()) {
          message = getStringValue(jsonResult, "PCD_PAY_MSG");
        }

        if ("success".equals(resultCode)
            || "A0000".equals(resultCode)
            || "T0000".equals(resultCode)) {
          log.info("✅ [페이플-{}] 성공 ({}ms) - {}", apiName, duration, message);
        } else {
          log.warn("⚠️ [페이플-{}] 실패 ({}ms) - {} : {}", apiName, duration, resultCode, message);
        }
      } else {
        log.info("✅ [페이플-{}] 성공 ({}ms)", apiName, duration);
      }
    } catch (Exception e) {
      log.info("✅ [페이플-{}] 성공 ({}ms) - 응답 파싱 실패", apiName, duration);
    }
  }

  /** 간소한 실패 로깅 */
  private void logSimpleError(String apiName, Exception error, long duration) {
    log.error("❌ [페이플-{}] 오류 ({}ms) - {}", apiName, duration, error.getMessage());
  }

  /** JSONObject에서 String 값을 안전하게 가져오는 헬퍼 메서드 */
  private String getStringValue(JSONObject json, String key) {
    Object value = json.get(key);
    return value != null ? String.valueOf(value) : "";
  }
}
