package liaison.groble.external.adapter.payment.aspect;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  /** PaypleService의 모든 메서드 호출을 가로채서 상세 로깅 */
  @Around("execution(* liaison.groble.external.adapter.payment.PaypleService.*(..))")
  public Object logPaypleApiCalls(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();
    String timestamp = LocalDateTime.now().format(formatter);
    String transactionId = generateTransactionId();

    // 요청 시작 로깅
    logRequestStart(methodName, args, timestamp, transactionId);

    long startTime = System.currentTimeMillis();
    try {
      Object result = joinPoint.proceed();
      long duration = System.currentTimeMillis() - startTime;

      // 성공 응답 로깅
      logSuccessResponse(methodName, result, duration, timestamp, transactionId);
      return result;

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;

      // 실패 응답 로깅
      logErrorResponse(methodName, e, duration, timestamp, transactionId);
      throw e;
    }
  }

  private void logRequestStart(
      String method, Object[] args, String timestamp, String transactionId) {
    log.info("┌─────────────────────────────────────────────────────────────────");
    log.info("│ 🚀 [PAYPLE-API] {} 요청 시작", method.toUpperCase());
    log.info("│ 📅 시각: {}", timestamp);
    log.info("│ 🆔 트랜잭션: {}", transactionId);
    log.info("├─────────────────────────────────────────────────────────────────");

    if (args != null && args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        Object arg = args[i];
        if (arg != null) {
          logRequestParameter(i, arg);
        }
      }
    }
    log.info("└─────────────────────────────────────────────────────────────────");
  }

  private void logRequestParameter(int index, Object param) {
    try {
      if (param instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> maskedParam = maskSensitiveData((Map<String, Object>) param);
        String prettyJson =
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(maskedParam);
        log.info("│ 📤 파라미터[{}]:\n{}", index, formatJsonForLog(prettyJson));
      } else {
        String maskedValue = maskSensitiveData(param.toString());
        log.info("│ 📤 파라미터[{}]: {}", index, maskedValue);
      }
    } catch (Exception e) {
      log.info("│ 📤 파라미터[{}]: {}", index, "[직렬화 실패]");
    }
  }

  private void logSuccessResponse(
      String method, Object result, long duration, String timestamp, String transactionId) {
    log.info("┌─────────────────────────────────────────────────────────────────");
    log.info("│ ✅ [PAYPLE-API] {} 응답 성공", method.toUpperCase());
    log.info("│ 🆔 트랜잭션: {}", transactionId);
    log.info("│ ⏱️  소요시간: {}ms", duration);
    log.info("├─────────────────────────────────────────────────────────────────");

    try {
      if (result instanceof JSONObject) {
        JSONObject jsonResult = (JSONObject) result;
        JSONObject maskedResult = maskSensitiveJsonData(jsonResult);
        log.info("│ 📥 응답 데이터:\n{}", formatJsonForLog(maskedResult.toJSONString()));

        // 결제 상태 정보 하이라이트
        highlightPaymentStatus(jsonResult);

      } else if (result != null) {
        log.info("│ 📥 응답: {}", result.toString());
      }
    } catch (Exception e) {
      log.info("│ 📥 응답: [파싱 실패]");
    }

    log.info("└─────────────────────────────────────────────────────────────────");
  }

  private void logErrorResponse(
      String method, Exception error, long duration, String timestamp, String transactionId) {
    log.error("┌─────────────────────────────────────────────────────────────────");
    log.error("│ ❌ [PAYPLE-API] {} 응답 실패", method.toUpperCase());
    log.error("│ 🆔 트랜잭션: {}", transactionId);
    log.error("│ ⏱️  소요시간: {}ms", duration);
    log.error("│ 🚨 오류: {}", error.getMessage());
    log.error("├─────────────────────────────────────────────────────────────────");
    log.error("│ 📥 오류 상세:", error);
    log.error("└─────────────────────────────────────────────────────────────────");
  }

  private void highlightPaymentStatus(JSONObject jsonResult) {
    try {
      String payResult = getStringValue(jsonResult, "PCD_PAY_RST");
      String resultMsg = getStringValue(jsonResult, "result_msg");
      String payOid = getStringValue(jsonResult, "PCD_PAY_OID");
      String payTotal = getStringValue(jsonResult, "PCD_PAY_TOTAL");

      if (!payResult.isEmpty()) {
        log.info("│ 💳 결제결과: {}", payResult);
      }
      if (!resultMsg.isEmpty()) {
        log.info("│ 📝 결과메시지: {}", resultMsg);
      }
      if (!payOid.isEmpty()) {
        log.info("│ 🔖 주문번호: {}", payOid);
      }
      if (!payTotal.isEmpty()) {
        log.info("│ 💰 결제금액: {}원", payTotal);
      }
    } catch (Exception e) {
      // 무시
    }
  }

  private Map<String, Object> maskSensitiveData(Map<String, Object> data) {
    Map<String, Object> masked = new HashMap<>(data);

    // 민감한 데이터 마스킹
    String[] sensitiveKeys = {
      "custKey", "PCD_CUST_KEY", "cst_id", "PCD_CST_ID", "PCD_AUTH_KEY", "PCD_PAY_REQKEY", "AuthKey"
    };

    for (String key : sensitiveKeys) {
      if (masked.containsKey(key)) {
        String value = String.valueOf(masked.get(key));
        masked.put(key, maskValue(value));
      }
    }

    return masked;
  }

  private JSONObject maskSensitiveJsonData(JSONObject json) {
    JSONObject masked = new JSONObject();

    for (Object keyObj : json.keySet()) {
      String key = String.valueOf(keyObj);
      Object value = json.get(key);

      if (isSensitiveKey(key)) {
        masked.put(key, maskValue(String.valueOf(value)));
      } else {
        masked.put(key, value);
      }
    }

    return masked;
  }

  private boolean isSensitiveKey(String key) {
    String[] sensitiveKeys = {
      "custKey", "PCD_CUST_KEY", "cst_id", "PCD_CST_ID", "PCD_AUTH_KEY", "PCD_PAY_REQKEY", "AuthKey"
    };

    for (String sensitiveKey : sensitiveKeys) {
      if (sensitiveKey.equalsIgnoreCase(key)) {
        return true;
      }
    }
    return false;
  }

  private String maskValue(String value) {
    if (value == null || value.length() <= 4) {
      return "****";
    }
    return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
  }

  private String maskSensitiveData(String data) {
    if (data == null || data.length() <= 4) {
      return "****";
    }
    return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
  }

  private String formatJsonForLog(String json) {
    // JSON을 로그에 예쁘게 표시하기 위해 각 줄에 │ 추가
    return json.replaceAll("(?m)^", "│   ");
  }

  private String generateTransactionId() {
    return String.format("TX_%d", System.currentTimeMillis() % 100000);
  }

  /** JSONObject에서 String 값을 안전하게 가져오는 헬퍼 메서드 */
  private String getStringValue(JSONObject json, String key) {
    Object value = json.get(key);
    return value != null ? String.valueOf(value) : "";
  }
}
