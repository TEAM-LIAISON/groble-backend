package liaison.groble.application.admin.settlement.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import liaison.groble.application.admin.settlement.dto.PaypleAccountVerificationRequest;
import liaison.groble.application.admin.settlement.dto.PayplePartnerAuthResult;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.external.adapter.payment.PaypleCodeGenerator;
import liaison.groble.external.adapter.payment.PaypleService;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 페이플 정산 서비스
 *
 * <p>페이플 정산지급대행 API와의 통신을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaypleSettlementService {

  private final PaypleService paypleService;
  private final PaypleConfig paypleConfig;
  private final PaypleCodeGenerator codeGenerator;

  /**
   * 파트너 인증 요청
   *
   * @return 파트너 인증 결과
   */
  @Retryable(
      value = PaypleApiException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public PayplePartnerAuthResult requestPartnerAuth() {
    log.info("페이플 파트너 인증 요청 시작");

    try {
      // 정산지급대행용 파트너 인증 코드 생성 (타임스탬프 기반)
      String authCode = generateSettlementAuthCode();

      // 파트너 인증 요청
      JSONObject authResult = paypleService.payAuthForSettlement(authCode);

      return buildPartnerAuthResult(authResult);

    } catch (Exception e) {
      log.error("페이플 파트너 인증 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 파트너 인증 실패", e);
    }
  }

  /**
   * 계좌 인증 요청
   *
   * @param request 계좌 인증 요청 정보
   * @param accessToken 파트너 인증으로 받은 액세스 토큰
   * @return 계좌 인증 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public JSONObject requestAccountVerification(
      PaypleAccountVerificationRequest request, String accessToken) {

    log.info(
        "페이플 계좌 인증 요청 - 계좌번호: {}, 금융기관: {}",
        maskAccountNumber(request.getAccountNum()),
        request.getBankCodeStd());

    try {
      // PaypleAccountVerificationRequest를 Map으로 변환
      Map<String, String> params = buildAccountVerificationParams(request);

      // 페이플 전용 계좌 검증 API 사용
      JSONObject result = paypleService.payAccountVerification(params);

      log.info("페이플 계좌 검증 요청 완료");
      return result;

    } catch (Exception e) {
      log.error("페이플 계좌 인증 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 계좌 인증 실패", e);
    }
  }

  /**
   * 빌링키로 이체 대기 요청
   *
   * @param billingTranId 계좌 인증으로 받은 빌링키
   * @param tranAmt 이체 금액
   * @param subId 하위 셀러 ID (선택)
   * @param printContent 거래 내역 표시 문구 (선택)
   * @return 이체 대기 요청 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000))
  public JSONObject requestTransfer(
      String billingTranId, String tranAmt, String subId, String printContent) {
    log.info("페이플 이체 대기 요청 - 빌링키: {}, 금액: {}", maskBillingKey(billingTranId), tranAmt);

    try {
      Map<String, String> params = buildTransferParams(billingTranId, tranAmt, subId, printContent);

      // 페이플 이체 대기 요청 API 호출
      JSONObject result = paypleService.payTransferRequest(params);

      log.info("페이플 이체 대기 요청 완료");
      return result;

    } catch (Exception e) {
      log.error("페이플 이체 대기 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 이체 대기 요청 실패", e);
    }
  }

  /**
   * 빌링키 그룹으로 이체 실행 요청
   *
   * @param groupKey 이체 대기 요청에서 받은 그룹키
   * @param billingTranId 실행할 빌링키 ("ALL" 또는 특정 빌링키)
   * @param accessToken 파트너 인증 토큰
   * @param webhookUrl 테스트 환경 웹훅 URL (선택)
   * @return 이체 실행 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000))
  public JSONObject requestTransferExecute(
      String groupKey, String billingTranId, String accessToken, String webhookUrl) {
    log.info("페이플 이체 실행 요청 - 그룹키: {}, 빌링키: {}", maskSensitiveData(groupKey), billingTranId);

    try {
      Map<String, String> params = buildTransferExecuteParams(groupKey, billingTranId, webhookUrl);

      // 페이플 이체 실행 API 호출
      JSONObject result = paypleService.payTransferExecute(params, accessToken);

      log.info("페이플 이체 실행 요청 완료");
      return result;

    } catch (Exception e) {
      log.error("페이플 이체 실행 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 이체 실행 요청 실패", e);
    }
  }

  /**
   * 빌링키 그룹의 이체 대기 취소 요청
   *
   * @param groupKey 이체 대기 요청에서 받은 그룹키
   * @param billingTranId 취소할 빌링키 ("ALL" 또는 특정 빌링키)
   * @param accessToken 파트너 인증 토큰
   * @param cancelReason 취소 사유 (선택)
   * @return 이체 취소 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000))
  public JSONObject requestTransferCancel(
      String groupKey, String billingTranId, String accessToken, String cancelReason) {
    log.info("페이플 이체 대기 취소 요청 - 그룹키: {}, 빌링키: {}", maskSensitiveData(groupKey), billingTranId);

    try {
      Map<String, String> params = buildTransferCancelParams(groupKey, billingTranId, cancelReason);

      // 페이플 이체 취소 API 호출
      JSONObject result = paypleService.payTransferCancel(params, accessToken);

      log.info("페이플 이체 대기 취소 요청 완료");
      return result;

    } catch (Exception e) {
      log.error("페이플 이체 대기 취소 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 이체 대기 취소 요청 실패", e);
    }
  }

  /**
   * 그룹 정산 요청
   *
   * @param settlementItems 정산 항목 목록
   * @param accessToken 파트너 인증으로 받은 액세스 토큰
   * @return 그룹 정산 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000))
  public JSONObject requestGroupSettlement(
      List<SettlementItem> settlementItems, String accessToken) {

    log.info("페이플 그룹 정산 요청 - 정산 항목 수: {}", settlementItems.size());

    try {
      Map<String, String> params = buildGroupSettlementParams(settlementItems, accessToken);

      // TODO: PaypleService에 그룹 정산 메서드 추가 필요
      JSONObject result = paypleService.payAuth(params);

      log.info("페이플 그룹 정산 요청 완료");
      return result;

    } catch (Exception e) {
      log.error("페이플 그룹 정산 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 그룹 정산 실패", e);
    }
  }

  /** 파트너 인증 결과 빌드 */
  private PayplePartnerAuthResult buildPartnerAuthResult(JSONObject authResult) {
    return PayplePartnerAuthResult.builder()
        .result(getString(authResult, "result"))
        .message(getString(authResult, "message"))
        .code(getString(authResult, "code"))
        .accessToken(getString(authResult, "access_token"))
        .tokenType(getString(authResult, "token_type"))
        .expiresIn(getString(authResult, "expires_in"))
        .build();
  }

  /** 계좌 인증 파라미터 빌드 */
  private Map<String, String> buildAccountVerificationParams(
      PaypleAccountVerificationRequest request) {
    Map<String, String> params = new HashMap<>();
    params.put("cst_id", request.getCstId());
    params.put("custKey", request.getCustKey());
    params.put("bank_code_std", request.getBankCodeStd());
    params.put("account_num", request.getAccountNum());
    params.put("account_holder_info_type", request.getAccountHolderInfoType());
    params.put("account_holder_info", request.getAccountHolderInfo());

    if (request.getSubId() != null) {
      params.put("sub_id", request.getSubId());
    }

    return params;
  }

  /** 이체 대기 요청 파라미터 빌드 */
  private Map<String, String> buildTransferParams(
      String billingTranId, String tranAmt, String subId, String printContent) {
    Map<String, String> params = new HashMap<>();
    params.put("cst_id", paypleConfig.getCstId());
    params.put("custKey", paypleConfig.getCustKey());
    params.put("billing_tran_id", billingTranId);
    params.put("tran_amt", tranAmt);

    if (subId != null && !subId.trim().isEmpty()) {
      params.put("sub_id", subId);
    }

    if (printContent != null && !printContent.trim().isEmpty()) {
      params.put("print_content", printContent);
    }

    // 중복 이체 방지를 위한 고유 키 생성 (UUID 기반)
    String distinctKey = generateDistinctKey();
    params.put("distinct_key", distinctKey);

    return params;
  }

  /** 이체 실행 요청 파라미터 빌드 */
  private Map<String, String> buildTransferExecuteParams(
      String groupKey, String billingTranId, String webhookUrl) {
    Map<String, String> params = new HashMap<>();
    params.put("cst_id", paypleConfig.getCstId());
    params.put("custKey", paypleConfig.getCustKey());
    params.put("group_key", groupKey);
    params.put("billing_tran_id", billingTranId);
    params.put("execute_type", "NOW");

    // 테스트 환경에서만 웹훅 URL 추가
    if (paypleConfig.isTestMode() && webhookUrl != null && !webhookUrl.trim().isEmpty()) {
      params.put("webhook_url", webhookUrl);
    }

    return params;
  }

  /** 이체 취소 요청 파라미터 빌드 */
  private Map<String, String> buildTransferCancelParams(
      String groupKey, String billingTranId, String cancelReason) {
    Map<String, String> params = new HashMap<>();
    params.put("cst_id", paypleConfig.getCstId());
    params.put("custKey", paypleConfig.getCustKey());
    params.put("group_key", groupKey);
    params.put("billing_tran_id", billingTranId);

    if (cancelReason != null && !cancelReason.trim().isEmpty()) {
      params.put("cancel_reason", cancelReason);
    }

    return params;
  }

  /** 그룹 정산 파라미터 빌드 */
  private Map<String, String> buildGroupSettlementParams(
      List<SettlementItem> settlementItems, String accessToken) {

    Map<String, String> params = new HashMap<>();
    params.put("access_token", accessToken);

    // 정산 항목들을 그룹화하여 파라미터 구성
    // TODO: 실제 페이플 그룹 정산 API 스펙에 따라 파라미터 구성 필요

    return params;
  }

  /** 정산용 인증 코드 생성 (타임스탬프 기반) */
  private String generateSettlementAuthCode() {
    return codeGenerator.generateSettlementCode();
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

  /** 빌링키 마스킹 (보안을 위한 로깅용) */
  private String maskBillingKey(String billingKey) {
    if (billingKey == null || billingKey.length() <= 8) {
      return "****";
    }
    return billingKey.substring(0, 4) + "****" + billingKey.substring(billingKey.length() - 4);
  }

  /** 민감한 데이터 마스킹 (그룹키, custKey 등) */
  private String maskSensitiveData(String sensitiveData) {
    if (sensitiveData == null || sensitiveData.length() <= 8) {
      return "****";
    }
    return sensitiveData.substring(0, 4)
        + "****"
        + sensitiveData.substring(sensitiveData.length() - 4);
  }

  /** 중복 이체 방지를 위한 고유 키 생성 */
  private String generateDistinctKey() {
    return java.util.UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
  }

  /** JSONObject에서 안전하게 문자열 추출 */
  private String getString(JSONObject json, String key) {
    Object value = json.get(key);
    return value != null ? value.toString() : null;
  }
}
