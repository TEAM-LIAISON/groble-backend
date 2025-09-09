package liaison.groble.external.adapter.payment;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import liaison.groble.external.adapter.payment.http.HttpClientAdapter;
import liaison.groble.external.adapter.payment.http.HttpClientException;
import liaison.groble.external.adapter.payment.http.HttpRequest;
import liaison.groble.external.adapter.payment.http.HttpResponse;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 리팩토링된 페이플 서비스 구현체
 *
 * <p>엔터프라이즈급 HTTP 클라이언트를 사용하여 페이플 API와 통신합니다.
 *
 * <p><strong>적용된 엔터프라이즈 패턴:</strong>
 *
 * <ul>
 *   <li>Adapter Pattern: HTTP 클라이언트 추상화
 *   <li>Template Method Pattern: 공통 요청 처리 플로우
 *   <li>Strategy Pattern: 다양한 HTTP 클라이언트 전략
 *   <li>Circuit Breaker Pattern: 장애 격리
 *   <li>Retry Pattern: 자동 재시도
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaypleServiceV2 implements PaypleService {

  private final PaypleConfig paypleConfig;
  private final HttpClientAdapter httpClient;
  private final JSONParser jsonParser = new JSONParser();

  @Override
  public JSONObject payAppCard(Map<String, String> params) {
    log.info(
        "페이플 앱카드 결제 요청 시작 - PCD_PAY_REQKEY: {}", maskSensitiveData(params.get("PCD_PAY_REQKEY")));

    try {
      // 요청 데이터 준비
      PayplePaymentRequest paymentRequest = createPaymentRequest(params);

      // HTTP 요청 실행
      HttpResponse response = executePaymentRequest(paymentRequest);

      // 응답 파싱 및 검증
      return parseAndValidateResponse(response);

    } catch (HttpClientException e) {
      log.error("페이플 앱카드 결제 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());

    } catch (ParseException e) {
      log.error("페이플 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");

    } catch (Exception e) {
      log.error("페이플 앱카드 결제 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  @Override
  public JSONObject payRefund(PaypleRefundRequest request) {
    log.info("페이플 결제 환불 요청 시작 - PCD_PAY_OID: {}", request.getPayOid());

    try {
      // HTTP 요청 실행
      HttpResponse response = executeRefundRequest(request);

      // 응답 파싱 및 검증
      return parseAndValidateResponse(response);

    } catch (HttpClientException e) {
      log.error("페이플 결제 환불 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());

    } catch (ParseException e) {
      log.error("페이플 환불 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");

    } catch (Exception e) {
      log.error("페이플 결제 환불 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  @Override
  public JSONObject payConfirm(Map<String, String> params) {
    log.info("페이플 결제 승인 요청 - 미구현");
    return createErrorResponse("NOT_IMPLEMENTED", "결제 승인 기능은 구현되지 않았습니다");
  }

  @Override
  public JSONObject payAuth(Map<String, String> params) {
    log.info("페이플 결제 인증 요청 시작 - cst_id: {}", maskSensitiveData(params.get("cst_id")));

    try {
      // HTTP 요청 실행
      HttpResponse response = executeAuthRequest(params);

      // 응답 파싱 및 검증
      return parseAndValidateResponse(response);

    } catch (HttpClientException e) {
      log.error("페이플 결제 인증 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());

    } catch (ParseException e) {
      log.error("페이플 인증 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");

    } catch (Exception e) {
      log.error("페이플 결제 인증 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  @Override
  public JSONObject payAuthForCancel() {
    log.info("페이플 결제 취소 인증 요청 시작");

    try {
      // 결제 취소 전용 파라미터 생성
      Map<String, String> cancelParams = createCancelAuthParams();
      HttpResponse response = executeAuthRequest(cancelParams);
      return parseAndValidateResponse(response);
    } catch (HttpClientException e) {
      log.error("페이플 결제 취소 인증 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());
    } catch (ParseException e) {
      log.error("페이플 취소 인증 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");
    } catch (Exception e) {
      log.error("페이플 결제 취소 인증 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  @Override
  public JSONObject payAuthForSettlement(String code) {
    log.info("페이플 정산지급대행 인증 요청 시작 - code: {}", maskSensitiveData(code));

    // code 검증
    if (!isValidSettlementCode(code)) {
      log.error("정산지급대행 code 형식 오류 - code: {}", code);
      return createErrorResponse("INVALID_CODE", "code는 영문+숫자 조합 10자리여야 합니다");
    }

    try {
      // 정산지급대행 전용 파라미터 생성
      Map<String, String> settlementParams = createSettlementAuthParams(code);
      HttpResponse response = executeSettlementAuthRequest(settlementParams);
      return parseAndValidateResponse(response);
    } catch (HttpClientException e) {
      log.error("페이플 정산지급대행 인증 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());
    } catch (ParseException e) {
      log.error("페이플 정산지급대행 인증 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");
    } catch (Exception e) {
      log.error("페이플 정산지급대행 인증 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  @Override
  public JSONObject paySimplePayment(Map<String, String> params) {
    log.info("페이플 간편결제 요청 - 미구현");
    return createErrorResponse("NOT_IMPLEMENTED", "간편결제 기능은 구현되지 않았습니다");
  }

  @Override
  public JSONObject payAccountVerification(Map<String, String> params) {
    log.info(
        "페이플 계좌 검증 요청 시작 - 계좌번호: {}, 은행코드: {}",
        maskAccountNumber(params.get("account_num")),
        params.get("bank_code_std"));

    try {
      HttpResponse response = executeAccountVerificationRequest(params);
      return parseAndValidateResponse(response);

    } catch (HttpClientException e) {
      log.error("페이플 계좌 검증 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());

    } catch (ParseException e) {
      log.error("페이플 계좌 검증 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");

    } catch (Exception e) {
      log.error("페이플 계좌 검증 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  @Override
  public JSONObject payTransferRequest(Map<String, String> params) {
    log.info(
        "페이플 이체 대기 요청 시작 - 빌링키: {}, 이체금액: {}",
        maskSensitiveData(params.get("billing_tran_id")),
        params.get("tran_amt"));

    try {
      HttpResponse response = executeTransferRequest(params);
      return parseAndValidateResponse(response);

    } catch (HttpClientException e) {
      log.error("페이플 이체 대기 요청 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());

    } catch (ParseException e) {
      log.error("페이플 이체 대기 요청 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");

    } catch (Exception e) {
      log.error("페이플 이체 대기 요청 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  @Override
  public JSONObject payTransferExecute(Map<String, String> params, String accessToken) {
    log.info(
        "페이플 이체 실행 요청 시작 - 그룹키: {}, 빌링키: {}",
        maskSensitiveData(params.get("group_key")),
        params.get("billing_tran_id"));

    try {
      HttpResponse response = executeTransferExecuteRequest(params, accessToken);
      return parseAndValidateResponse(response);

    } catch (HttpClientException e) {
      log.error("페이플 이체 실행 요청 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());

    } catch (ParseException e) {
      log.error("페이플 이체 실행 요청 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");

    } catch (Exception e) {
      log.error("페이플 이체 실행 요청 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  @Override
  public JSONObject payTransferCancel(Map<String, String> params, String accessToken) {
    log.info(
        "페이플 이체 대기 취소 요청 시작 - 그룹키: {}, 빌링키: {}",
        maskSensitiveData(params.get("group_key")),
        params.get("billing_tran_id"));

    try {
      HttpResponse response = executeTransferCancelRequest(params, accessToken);
      return parseAndValidateResponse(response);

    } catch (HttpClientException e) {
      log.error("페이플 이체 대기 취소 HTTP 요청 실패", e);
      return createErrorResponse("NETWORK_ERROR", "네트워크 오류가 발생했습니다: " + e.getMessage());

    } catch (ParseException e) {
      log.error("페이플 이체 대기 취소 응답 파싱 실패", e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다");

    } catch (Exception e) {
      log.error("페이플 이체 대기 취소 예상치 못한 오류", e);
      return createErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다");
    }
  }

  private PayplePaymentRequest createPaymentRequest(Map<String, String> params) {
    return PayplePaymentRequest.builder()
        .url(params.get("PCD_PAY_COFURL"))
        .cstId(params.get("PCD_CST_ID"))
        .custKey(params.get("PCD_CUST_KEY"))
        .authKey(params.get("PCD_AUTH_KEY"))
        .payReqKey(params.get("PCD_PAY_REQKEY"))
        .build();
  }

  private PaypleRefundRequest createRefundRequest(Map<String, String> params) {
    return PaypleRefundRequest.builder()
        .url(paypleConfig.getCancelApiUrl()) // 올바른 환불 API URL 사용
        .cstId(paypleConfig.getCstId())
        .custKey(paypleConfig.getCustKey())
        .authKey("test") // TODO: AuthKey 설정 필요
        .payOid(params.get("PCD_PAY_OID"))
        .refundTotal(params.get("PCD_REFUND_TOTAL"))
        .refundTaxfree(params.getOrDefault("PCD_REFUND_TAXFREE", "0"))
        .refundReason(params.get("PCD_REFUND_REASON"))
        .build();
  }

  private HttpResponse executePaymentRequest(PayplePaymentRequest request)
      throws HttpClientException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("PCD_CST_ID", request.getCstId());
    requestBody.put("PCD_CUST_KEY", request.getCustKey());
    requestBody.put("PCD_AUTH_KEY", request.getAuthKey());
    requestBody.put("PCD_PAY_REQKEY", request.getPayReqKey());

    log.debug("페이플 결제 요청 본문: {}", requestBody.toJSONString());

    HttpRequest httpRequest = HttpRequest.post(request.getUrl(), requestBody.toJSONString());
    return httpClient.post(httpRequest);
  }

  private HttpResponse executeRefundRequest(PaypleRefundRequest request)
      throws HttpClientException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("PCD_CST_ID", request.getCstId());
    requestBody.put("PCD_CUST_KEY", request.getCustKey());
    requestBody.put("PCD_AUTH_KEY", request.getAuthKey());
    requestBody.put("PCD_REFUND_KEY", paypleConfig.getRefundKey());
    requestBody.put("PCD_PAYCANCEL_FLAG", "Y");
    requestBody.put("PCD_PAY_OID", request.getPayOid());
    requestBody.put(
        "PCD_PAY_DATE", new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()));
    requestBody.put("PCD_REFUND_TOTAL", request.getRefundTotal());

    if (request.getRefundTaxfree() != null && !request.getRefundTaxfree().equals("0")) {
      requestBody.put("PCD_REFUND_TAXTOTAL", request.getRefundTaxfree());
    }

    if (request.getRefundReason() != null) {
      requestBody.put("PCD_REFUND_REASON", request.getRefundReason());
    }

    log.debug("페이플 환불 요청 본문: {}", requestBody.toJSONString());

    HttpRequest httpRequest = HttpRequest.post(request.getUrl(), requestBody.toJSONString());
    return httpClient.post(httpRequest);
  }

  private HttpResponse executeAuthRequest(Map<String, String> params) throws HttpClientException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("cst_id", paypleConfig.getCstId());
    requestBody.put("custKey", paypleConfig.getCustKey());

    // 추가 파라미터 설정
    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        // 기본 설정값은 덮어쓰지 않음
        if (!"cst_id".equals(entry.getKey()) && !"custKey".equals(entry.getKey())) {
          requestBody.put(entry.getKey(), entry.getValue());
        }
      }
    }

    log.debug("페이플 인증 요청 본문: {}", requestBody.toJSONString());

    String authUrl = paypleConfig.getAuthApiUrl();

    Map<String, String> headers = new HashMap<>();
    headers.put("content-type", "application/json");
    headers.put("charset", "UTF-8");
    headers.put("referer", paypleConfig.getRefererUrl());

    HttpRequest httpRequest =
        HttpRequest.postWithHeaders(authUrl, headers, requestBody.toJSONString());

    return httpClient.post(httpRequest);
  }

  private HttpResponse executeSettlementAuthRequest(Map<String, String> params)
      throws HttpClientException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("cst_id", paypleConfig.getCstId());
    requestBody.put("custKey", paypleConfig.getCustKey());

    // 추가 파라미터 설정
    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        // 기본 설정값은 덮어쓰지 않음
        if (!"cst_id".equals(entry.getKey()) && !"custKey".equals(entry.getKey())) {
          requestBody.put(entry.getKey(), entry.getValue());
        }
      }
    }

    log.debug("페이플 정산지급대행 인증 요청 본문: {}", requestBody.toJSONString());

    String authUrl = paypleConfig.getSettlementAuthApiUrl();

    Map<String, String> headers = new HashMap<>();
    headers.put("content-type", "application/json");
    headers.put("charset", "UTF-8");
    headers.put("referer", paypleConfig.getRefererUrl());

    HttpRequest httpRequest =
        HttpRequest.postWithHeaders(authUrl, headers, requestBody.toJSONString());

    return httpClient.post(httpRequest);
  }

  private HttpResponse executeAccountVerificationRequest(Map<String, String> params)
      throws HttpClientException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("cst_id", params.get("cst_id"));
    requestBody.put("custKey", params.get("custKey"));
    requestBody.put("bank_code_std", params.get("bank_code_std"));
    requestBody.put("account_num", params.get("account_num"));
    requestBody.put("account_holder_info_type", params.get("account_holder_info_type"));
    requestBody.put("account_holder_info", params.get("account_holder_info"));

    if (params.get("sub_id") != null) {
      requestBody.put("sub_id", params.get("sub_id"));
    }

    log.debug("페이플 계좌 검증 요청 본문: {}", maskSensitiveRequestBody(requestBody));

    // 계좌 검증 전용 URL - PaypleConfig에 추가 필요
    String accountVerificationUrl = paypleConfig.getAccountVerificationUrl();

    Map<String, String> headers = new HashMap<>();
    headers.put("content-type", "application/json");
    headers.put("charset", "UTF-8");
    headers.put("referer", paypleConfig.getRefererUrl());

    HttpRequest httpRequest =
        HttpRequest.postWithHeaders(accountVerificationUrl, headers, requestBody.toJSONString());
    return httpClient.post(httpRequest);
  }

  private HttpResponse executeTransferRequest(Map<String, String> params)
      throws HttpClientException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("cst_id", params.get("cst_id"));
    requestBody.put("custKey", params.get("custKey"));
    requestBody.put("billing_tran_id", params.get("billing_tran_id"));
    requestBody.put("tran_amt", params.get("tran_amt"));

    // sub_id는 선택적 파라미터
    if (params.get("sub_id") != null) {
      requestBody.put("sub_id", params.get("sub_id"));
    }

    // distinct_key는 선택적 파라미터 (중복 이체 방지)
    if (params.get("distinct_key") != null) {
      requestBody.put("distinct_key", params.get("distinct_key"));
    }

    // print_content는 선택적 파라미터 (거래 내역 표시 문구)
    if (params.get("print_content") != null) {
      requestBody.put("print_content", params.get("print_content"));
    }

    log.debug("페이플 이체 대기 요청 본문: {}", maskSensitiveTransferRequestBody(requestBody));

    String transferRequestUrl = paypleConfig.getPendingTransferRequestUrl();

    Map<String, String> headers = new HashMap<>();
    headers.put("content-type", "application/json");
    headers.put("charset", "UTF-8");
    headers.put("referer", paypleConfig.getRefererUrl());

    HttpRequest httpRequest =
        HttpRequest.postWithHeaders(transferRequestUrl, headers, requestBody.toJSONString());
    return httpClient.post(httpRequest);
  }

  private HttpResponse executeTransferExecuteRequest(Map<String, String> params, String accessToken)
      throws HttpClientException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("cst_id", params.get("cst_id"));
    requestBody.put("custKey", params.get("custKey"));
    requestBody.put("group_key", params.get("group_key"));
    requestBody.put("billing_tran_id", params.get("billing_tran_id"));
    requestBody.put("execute_type", params.getOrDefault("execute_type", "NOW"));

    // 테스트 환경에서만 webhook_url 추가
    if (paypleConfig.isTestMode() && params.get("webhook_url") != null) {
      requestBody.put("webhook_url", params.get("webhook_url"));
    }

    log.debug("페이플 이체 실행 요청 본문: {}", maskSensitiveTransferExecuteRequestBody(requestBody));

    String transferExecuteUrl = paypleConfig.getTransferExecuteUrl();

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + accessToken);
    headers.put("Content-Type", "application/json");
    headers.put("Cache-Control", "no-cache");

    HttpRequest httpRequest =
        HttpRequest.postWithHeaders(transferExecuteUrl, headers, requestBody.toJSONString());
    return httpClient.post(httpRequest);
  }

  private HttpResponse executeTransferCancelRequest(Map<String, String> params, String accessToken)
      throws HttpClientException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("cst_id", params.get("cst_id"));
    requestBody.put("custKey", params.get("custKey"));
    requestBody.put("group_key", params.get("group_key"));
    requestBody.put("billing_tran_id", params.get("billing_tran_id"));

    // 취소 사유 (선택적)
    if (params.get("cancel_reason") != null) {
      requestBody.put("cancel_reason", params.get("cancel_reason"));
    }

    log.debug("페이플 이체 대기 취소 요청 본문: {}", maskSensitiveTransferExecuteRequestBody(requestBody));

    String transferCancelUrl = paypleConfig.getTransferCancelUrl();

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + accessToken);
    headers.put("Content-Type", "application/json");
    headers.put("Cache-Control", "no-cache");

    HttpRequest httpRequest =
        HttpRequest.postWithHeaders(transferCancelUrl, headers, requestBody.toJSONString());
    return httpClient.post(httpRequest);
  }

  private JSONObject parseAndValidateResponse(HttpResponse response) throws ParseException {
    if (!response.isSuccess()) {
      log.warn(
          "페이플 API 응답 오류 - 상태코드: {}, 응답시간: {}ms",
          response.getStatusCode(),
          response.getResponseTimeMs());
      return createErrorResponse(
          "API_ERROR", "페이플 API 오류 (상태코드: " + response.getStatusCode() + ")");
    }

    String responseBody = response.getBody();

    // 응답 본문 검증 및 로깅
    if (responseBody == null || responseBody.trim().isEmpty()) {
      log.error("페이플 API 응답이 비어있음 - 상태코드: {}", response.getStatusCode());
      return createErrorResponse("EMPTY_RESPONSE", "페이플 API 응답이 비어있습니다");
    }

    log.debug("페이플 API 응답 원문 길이: {}, 내용: {}", responseBody.length(), responseBody);

    try {
      JSONObject jsonResponse = (JSONObject) jsonParser.parse(responseBody);

      log.info(
          "페이플 API 응답 성공 - 응답시간: {}ms, 결과: {}",
          response.getResponseTimeMs(),
          jsonResponse.getOrDefault("PCD_PAY_RST", "UNKNOWN"));

      return jsonResponse;

    } catch (ParseException e) {
      log.error(
          "페이플 API 응답 JSON 파싱 실패 - 응답 길이: {}, 응답 내용: [{}]", responseBody.length(), responseBody, e);
      return createErrorResponse("PARSE_ERROR", "응답 파싱 중 오류가 발생했습니다: " + responseBody);
    }
  }

  private JSONObject createErrorResponse(String errorCode, String errorMessage) {
    JSONObject errorResponse = new JSONObject();
    errorResponse.put("PCD_PAY_RST", "error");
    errorResponse.put("PCD_PAY_CODE", errorCode);
    errorResponse.put("PCD_PAY_MSG", errorMessage);
    return errorResponse;
  }

  private String maskSensitiveData(String sensitiveData) {
    if (sensitiveData == null || sensitiveData.length() <= 8) {
      return "***";
    }
    return sensitiveData.substring(0, 4)
        + "***"
        + sensitiveData.substring(sensitiveData.length() - 4);
  }

  /** 결제 취소용 인증 파라미터 생성 필요한 파라미터: cst_id, custKey, PCD_PAYCANCEL_FLAG */
  private Map<String, String> createCancelAuthParams() {
    Map<String, String> params = new HashMap<>();
    params.put("cst_id", paypleConfig.getCstId());
    params.put("custKey", paypleConfig.getCustKey());
    params.put("PCD_PAYCANCEL_FLAG", "Y");
    return params;
  }

  /** 정산지급대행용 인증 파라미터 생성 필요한 파라미터: cst_id, custKey, code */
  private Map<String, String> createSettlementAuthParams(String code) {
    Map<String, String> params = new HashMap<>();
    params.put("cst_id", paypleConfig.getCstId());
    params.put("custKey", paypleConfig.getCustKey());
    params.put("code", code);
    return params;
  }

  /** 정산지급대행 code 유효성 검증 영문+숫자 조합 10자리인지 확인 */
  private boolean isValidSettlementCode(String code) {
    if (code == null || code.length() != 10) {
      return false;
    }
    // 영문+숫자 조합인지 확인
    return code.matches("^[a-zA-Z0-9]{10}$");
  }

  /** 계좌번호 마스킹 (보안을 위한 로깅용) */
  private String maskAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.length() <= 4) {
      return "****";
    }
    return accountNumber.substring(0, 2)
        + "****"
        + accountNumber.substring(accountNumber.length() - 2);
  }

  /** 민감한 정보가 포함된 요청 본문 마스킹 */
  private String maskSensitiveRequestBody(JSONObject requestBody) {
    JSONObject maskedBody = new JSONObject();
    for (Object key : requestBody.keySet()) {
      String keyStr = key.toString();
      Object value = requestBody.get(key);

      if ("account_num".equals(keyStr)) {
        maskedBody.put(keyStr, maskAccountNumber(value.toString()));
      } else if ("custKey".equals(keyStr) || "account_holder_info".equals(keyStr)) {
        maskedBody.put(keyStr, maskSensitiveData(value.toString()));
      } else {
        maskedBody.put(keyStr, value);
      }
    }
    return maskedBody.toJSONString();
  }

  /** 이체 요청의 민감한 정보가 포함된 요청 본문 마스킹 */
  private String maskSensitiveTransferRequestBody(JSONObject requestBody) {
    JSONObject maskedBody = new JSONObject();
    for (Object key : requestBody.keySet()) {
      String keyStr = key.toString();
      Object value = requestBody.get(key);

      if ("billing_tran_id".equals(keyStr)) {
        maskedBody.put(keyStr, maskSensitiveData(value.toString()));
      } else if ("custKey".equals(keyStr)) {
        maskedBody.put(keyStr, maskSensitiveData(value.toString()));
      } else {
        maskedBody.put(keyStr, value);
      }
    }
    return maskedBody.toJSONString();
  }

  /** 이체 실행 요청의 민감한 정보가 포함된 요청 본문 마스킹 */
  private String maskSensitiveTransferExecuteRequestBody(JSONObject requestBody) {
    JSONObject maskedBody = new JSONObject();
    for (Object key : requestBody.keySet()) {
      String keyStr = key.toString();
      Object value = requestBody.get(key);

      if ("group_key".equals(keyStr) || "custKey".equals(keyStr)) {
        maskedBody.put(keyStr, maskSensitiveData(value.toString()));
      } else {
        maskedBody.put(keyStr, value);
      }
    }
    return maskedBody.toJSONString();
  }
}
