package liaison.groble.external.adapter.payment.http;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Payple HTTP 요청/응답을 상세 로깅하는 인터셉터
 *
 * <p>HTTP 레벨에서의 raw 데이터를 예쁘게 로깅합니다.
 */
@Component
@Slf4j
public class PaypleHttpLoggingInterceptor {

  private final ObjectMapper objectMapper = new ObjectMapper();

  /** HTTP 요청 로깅 */
  public void logHttpRequest(HttpRequest request) {
    if (!isPaypleRequest(request.getUrl())) {
      return;
    }

    log.info("┌─────────────────── HTTP REQUEST ───────────────────");
    log.info("│ 🌐 URL: {}", request.getUrl());
    log.info("│ 📋 METHOD: {}", request.getMethod());

    // 헤더 로깅
    if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
      log.info("│ 📋 HEADERS:");
      request
          .getHeaders()
          .forEach(
              (key, value) -> {
                String maskedValue = isSensitiveHeader(key) ? maskValue(value) : value;
                log.info("│   {} = {}", key, maskedValue);
              });
    }

    // 바디 로깅
    if (request.getBody() != null && !request.getBody().isEmpty()) {
      log.info("│ 📤 REQUEST BODY:");
      logJsonBody(request.getBody());
    }

    log.info("└────────────────────────────────────────────────────");
  }

  /** HTTP 응답 로깅 */
  public void logHttpResponse(HttpResponse response, String requestUrl) {
    if (!isPaypleRequest(requestUrl)) {
      return;
    }

    log.info("┌─────────────────── HTTP RESPONSE ──────────────────");
    log.info("│ 📊 STATUS: {}", response.getStatusCode());
    log.info("│ ⏱️  TIME: {}ms", response.getResponseTimeMs());

    // 응답 헤더 로깅 (주요한 것만)
    if (response.getHeaders() != null) {
      String contentType = response.getHeaders().get("Content-Type");
      if (contentType != null) {
        log.info("│ 📋 Content-Type: {}", contentType);
      }
    }

    // 응답 바디 로깅
    if (response.getBody() != null && !response.getBody().isEmpty()) {
      log.info("│ 📥 RESPONSE BODY:");
      logJsonBody(response.getBody());
    }

    log.info("└────────────────────────────────────────────────────");
  }

  /** HTTP 오류 로깅 */
  public void logHttpError(String requestUrl, Exception error, long responseTimeMs) {
    if (!isPaypleRequest(requestUrl)) {
      return;
    }

    log.error("┌─────────────────── HTTP ERROR ─────────────────────");
    log.error("│ 🌐 URL: {}", requestUrl);
    log.error("│ ⏱️  TIME: {}ms", responseTimeMs);
    log.error("│ 🚨 ERROR: {}", error.getMessage());
    log.error("│ 📋 ERROR TYPE: {}", error.getClass().getSimpleName());
    log.error("└────────────────────────────────────────────────────");
  }

  private void logJsonBody(String body) {
    try {
      JsonNode jsonNode = objectMapper.readTree(body);
      JsonNode maskedNode = maskSensitiveJsonFields(jsonNode);
      String prettyJson =
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(maskedNode);

      // 각 줄에 │ 추가하여 로그 포맷 맞추기
      String[] lines = prettyJson.split("\n");
      for (String line : lines) {
        log.info("│   {}", line);
      }
    } catch (Exception e) {
      // JSON 파싱 실패 시 원본 텍스트 그대로 출력
      log.info("│   {}", body);
    }
  }

  private JsonNode maskSensitiveJsonFields(JsonNode node) {
    if (node.isObject()) {
      ObjectMapper mapper = new ObjectMapper();
      com.fasterxml.jackson.databind.node.ObjectNode objectNode =
          (com.fasterxml.jackson.databind.node.ObjectNode) node;

      String[] sensitiveFields = {
        "custKey",
        "PCD_CUST_KEY",
        "cst_id",
        "PCD_CST_ID",
        "PCD_AUTH_KEY",
        "PCD_PAY_REQKEY",
        "AuthKey"
      };

      for (String field : sensitiveFields) {
        if (objectNode.has(field)) {
          String originalValue = objectNode.get(field).asText();
          objectNode.put(field, maskValue(originalValue));
        }
      }
    }
    return node;
  }

  private boolean isPaypleRequest(String url) {
    return url != null && (url.contains("payple.kr") || url.contains("democpay"));
  }

  private boolean isSensitiveHeader(String headerName) {
    return "authorization".equalsIgnoreCase(headerName) || "referer".equalsIgnoreCase(headerName);
  }

  private String maskValue(String value) {
    if (value == null || value.length() <= 4) {
      return "****";
    }
    return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
  }
}
