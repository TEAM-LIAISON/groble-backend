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
      Map<String, String> params = buildAccountVerificationParams(request);

      // TODO: PaypleService에 계좌 인증 메서드 추가 필요
      // 현재는 임시로 payAuth를 사용하지만, 실제로는 계좌 인증 전용 API 필요
      JSONObject result = paypleService.payAuth(params);

      log.info("페이플 계좌 인증 요청 완료");
      return result;

    } catch (Exception e) {
      log.error("페이플 계좌 인증 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 계좌 인증 실패", e);
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
    return "SETTLEMENT_" + System.currentTimeMillis() % 1000000;
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

  /** JSONObject에서 안전하게 문자열 추출 */
  private String getString(JSONObject json, String key) {
    Object value = json.get(key);
    return value != null ? value.toString() : null;
  }
}
