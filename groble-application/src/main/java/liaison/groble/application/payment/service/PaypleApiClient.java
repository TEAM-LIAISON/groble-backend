package liaison.groble.application.payment.service;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import liaison.groble.application.payment.dto.PaymentCancelInfo;
import liaison.groble.application.payment.dto.PaypleApprovalResult;
import liaison.groble.application.payment.dto.PaypleAuthResponseDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.PaypleRefundResult;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.application.payment.util.CardQuotaNormalizer;
import liaison.groble.external.adapter.payment.PaypleCodeGenerator;
import liaison.groble.external.adapter.payment.PaypleRefundRequest;
import liaison.groble.external.adapter.payment.PaypleService;
import liaison.groble.external.adapter.payment.PaypleSimplePayRequest;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 페이플 API 클라이언트
 *
 * <p>페이플 외부 API와의 통신을 담당합니다. 재시도 로직과 에러 핸들링을 포함합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaypleApiClient {
  private static final String RESULT_SUCCESS = "success";

  private final PaypleService paypleService;
  private final PaypleConfig paypleConfig;
  private final PaypleCodeGenerator codeGenerator;

  /**
   * 일반 결제 인증 요청 (기존 호환성 유지)
   *
   * @param payWork 작업 구분 (AUTH, LINKREG, PAY 등)
   * @return 인증 응답
   */
  @Retryable(
      value = PaypleApiException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public PaypleAuthResponseDTO requestAuth(String payWork) {
    log.info("페이플 일반 결제 인증 요청 - payWork: {}", payWork);

    Map<String, String> params = new HashMap<>();
    params.put("PCD_PAY_WORK", payWork);

    try {
      JSONObject authResult = paypleService.payAuth(params);
      return buildAuthResponse(authResult, "일반 결제 인증");

    } catch (Exception e) {
      log.error("페이플 일반 결제 인증 중 오류 발생", e);
      throw new PaypleApiException("페이플 일반 결제 인증 실패", e);
    }
  }

  /**
   * 결제 취소를 위한 전용 인증 요청
   *
   * @return 인증 응답
   */
  @Retryable(
      value = PaypleApiException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public PaypleAuthResponseDTO requestAuthForCancel() {
    log.info("페이플 결제 취소 전용 인증 요청");

    try {
      JSONObject authResult = paypleService.payAuthForCancel();
      return buildAuthResponse(authResult, "결제 취소 인증");

    } catch (Exception e) {
      log.error("페이플 결제 취소 인증 중 오류 발생", e);
      throw new PaypleApiException("페이플 결제 취소 인증 실패", e);
    }
  }

  /**
   * 정산지급대행을 위한 계정 인증 요청
   *
   * @param customCode 사용자 지정 코드 (null인 경우 자동 생성)
   * @return 인증 응답
   */
  @Retryable(
      value = PaypleApiException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public PaypleAuthResponseDTO requestAuthForSettlementAccount(String customCode) {
    String code = (customCode != null) ? customCode : codeGenerator.generateSettlementCode();
    log.info("페이플 정산지급대행 계정 인증 요청 - code: {}", maskCode(code));

    try {
      JSONObject authResult = paypleService.payAuthForSettlement(code);
      return buildAuthResponse(authResult, "정산지급대행 계정 인증");

    } catch (Exception e) {
      log.error("페이플 정산지급대행 계정 인증 중 오류 발생", e);
      throw new PaypleApiException("페이플 정산지급대행 계정 인증 실패", e);
    }
  }

  /**
   * 정산지급대행을 위한 계좌 인증 요청 타임스탬프 기반 코드 사용
   *
   * @return 인증 응답
   */
  @Retryable(
      value = PaypleApiException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public PaypleAuthResponseDTO requestAuthForSettlementBank() {
    String timestampCode = codeGenerator.generateTimestampBasedCode();
    log.info("페이플 정산지급대행 계좌 인증 요청 - timestamp code: {}", timestampCode);

    try {
      JSONObject authResult = paypleService.payAuthForSettlement(timestampCode);
      return buildAuthResponse(authResult, "정산지급대행 계좌 인증");

    } catch (Exception e) {
      log.error("페이플 정산지급대행 계좌 인증 중 오류 발생", e);
      throw new PaypleApiException("페이플 정산지급대행 계좌 인증 실패", e);
    }
  }

  /**
   * 앱카드 결제 승인 요청
   *
   * @param authResult 인증 결과
   * @return 승인 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public PaypleApprovalResult requestApproval(PaypleAuthResultDTO authResult) {
    log.info("페이플 승인 요청 - merchantUid: {}", authResult.getPayOid());

    Map<String, String> params = new HashMap<>();
    params.put("PCD_CST_ID", paypleConfig.getCstId());
    params.put("PCD_CUST_KEY", paypleConfig.getCustKey());
    params.put("PCD_AUTH_KEY", authResult.getAuthKey());
    params.put("PCD_PAY_REQKEY", authResult.getPayReqKey());
    params.put("PCD_PAY_COFURL", authResult.getPayCofUrl());
    params.put("PCD_PAY_CARDQUOTA", CardQuotaNormalizer.normalize(authResult.getPayCardQuota()));
    if (StringUtils.hasText(authResult.getPayerId())) {
      params.put("PCD_PAYER_ID", authResult.getPayerId());
    }
    if (StringUtils.hasText(authResult.getPayType())) {
      params.put("PCD_PAY_TYPE", authResult.getPayType());
    }
    if (StringUtils.hasText(authResult.getPayGoods())) {
      params.put("PCD_PAY_GOODS", authResult.getPayGoods());
    }
    if (StringUtils.hasText(authResult.getPayTotal())) {
      params.put("PCD_PAY_TOTAL", authResult.getPayTotal());
    }
    if (StringUtils.hasText(authResult.getSimpleFlag())) {
      params.put("PCD_SIMPLE_FLAG", authResult.getSimpleFlag());
    }

    try {
      JSONObject approvalResult = paypleService.payAppCard(params);
      PaypleApprovalResult result = buildApprovalResult(approvalResult);

      log.info(
          "페이플 승인 응답 - merchantUid: {}, result: {}", authResult.getPayOid(), result.getPayRst());

      return result;

    } catch (Exception e) {
      log.error("페이플 승인 요청 중 오류 발생 - merchantUid: {}", authResult.getPayOid(), e);
      throw new PaypleApiException("페이플 승인 요청 실패", e);
    }
  }

  public PaypleApprovalResult requestSimplePayment(PaypleSimplePayRequest request) {
    log.info("페이플 빌링키 결제 요청 - merchantUid: {}", request.getPayOid());

    try {
      PaypleAuthResponseDTO authResponse = requestAuth("PAY");
      JSONObject approvalResult =
          paypleService.paySimplePayment(request, authResponse.getAuthKey());
      PaypleApprovalResult result = buildApprovalResult(approvalResult);
      log.info(
          "페이플 빌링키 결제 응답 - merchantUid: {}, result: {}", request.getPayOid(), result.getPayRst());
      return result;
    } catch (PaypleApiException e) {
      throw e;
    } catch (Exception e) {
      log.error("페이플 빌링키 결제 중 오류 발생 - merchantUid: {}", request.getPayOid(), e);
      throw new PaypleApiException("페이플 빌링키 결제 실패", e);
    }
  }

  /**
   * 앱카드 환불 요청
   *
   * @param cancelInfo 취소 정보
   * @return 환불 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public PaypleRefundResult requestRefund(PaymentCancelInfo cancelInfo) {
    log.info(
        "페이플 환불 요청 - merchantUid: {}, amount: {}",
        cancelInfo.getMerchantUid(),
        cancelInfo.getRefundAmount());

    try {
      // 1. 환불을 위한 전용 인증 요청
      PaypleAuthResponseDTO authResponse = requestAuthForCancel();

      // 2. 환불 요청 생성
      PaypleRefundRequest refundRequest =
          PaypleRefundRequest.builder()
              .url(paypleConfig.getCancelApiUrl())
              .cstId(paypleConfig.getCstId())
              .custKey(paypleConfig.getCustKey())
              .authKey(authResponse.getAuthKey())
              .payOid(cancelInfo.getMerchantUid())
              .refundTotal(cancelInfo.getRefundAmount().toString())
              .refundTaxfree(
                  cancelInfo.getRefundTaxAmount() != null
                      ? cancelInfo.getRefundTaxAmount().toString()
                      : null)
              .refundReason("사용자 요청")
              .build();

      // 3. 환불 API 호출
      JSONObject refundResult = paypleService.payRefund(refundRequest);

      String refundRst = getString(refundResult, "PCD_PAY_RST");
      boolean isSuccess = RESULT_SUCCESS.equalsIgnoreCase(refundRst);

      log.info("페이플 환불 응답 - merchantUid: {}, result: {}", cancelInfo.getMerchantUid(), refundRst);

      return PaypleRefundResult.builder()
          .success(isSuccess)
          .payRst(refundRst)
          .payCode(getString(refundResult, "PCD_PAY_CODE"))
          .payMsg(getString(refundResult, "PCD_PAY_MSG"))
          .refundOid(getString(refundResult, "PCD_REFUND_OID"))
          .refundTotal(getString(refundResult, "PCD_REFUND_TOTAL"))
          .errorCode(isSuccess ? null : getString(refundResult, "PCD_PAY_CODE"))
          .errorMessage(isSuccess ? null : getString(refundResult, "PCD_PAY_MSG"))
          .build();

    } catch (PaypleApiException e) {
      throw e;
    } catch (Exception e) {
      log.error("페이플 환불 요청 중 오류 발생 - merchantUid: {}", cancelInfo.getMerchantUid(), e);
      throw new PaypleApiException("페이플 환불 요청 실패", e);
    }
  }

  /**
   * 정산지급대행 계좌 검증 요청
   *
   * @return 계좌 검증 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public PaypleAuthResponseDTO requestAccountVerification() {
    log.info("페이플 정산지급대행 계좌 검증 요청");

    try {
      // 정산지급대행 계좌 인증 사용
      return requestAuthForSettlementBank();

    } catch (Exception e) {
      log.error("페이플 계좌 검증 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 계좌 검증 요청 실패", e);
    }
  }

  /**
   * 정산지급대행 계정 등록 요청
   *
   * @param merchantCode 상점 고유 코드
   * @return 계정 등록 결과
   */
  @Retryable(value = PaypleApiException.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
  public PaypleAuthResponseDTO requestAccountRegistration(String merchantCode) {
    log.info("페이플 정산지급대행 계정 등록 요청 - merchantCode: {}", maskCode(merchantCode));

    try {
      // 상점별 고유 코드로 정산지급대행 계정 인증
      return requestAuthForSettlementAccount(merchantCode);

    } catch (Exception e) {
      log.error("페이플 계정 등록 요청 중 오류 발생", e);
      throw new PaypleApiException("페이플 계정 등록 요청 실패", e);
    }
  }

  private PaypleApprovalResult buildApprovalResult(JSONObject approvalResult) {
    String payRst = getString(approvalResult, "PCD_PAY_RST");
    boolean isSuccess = RESULT_SUCCESS.equalsIgnoreCase(payRst);

    return PaypleApprovalResult.builder()
        .success(isSuccess)
        .payRst(payRst)
        .payCode(getString(approvalResult, "PCD_PAY_CODE"))
        .payMsg(getString(approvalResult, "PCD_PAY_MSG"))
        .payOid(getString(approvalResult, "PCD_PAY_OID"))
        .payType(getString(approvalResult, "PCD_PAY_TYPE"))
        .payTime(getString(approvalResult, "PCD_PAY_TIME"))
        .payTotal(getString(approvalResult, "PCD_PAY_TOTAL"))
        .payTaxTotal(getString(approvalResult, "PCD_PAY_TAXTOTAL"))
        .payIsTax(getString(approvalResult, "PCD_PAY_ISTAX"))
        .payGoods(getString(approvalResult, "PCD_PAY_GOODS"))
        .payerName(getString(approvalResult, "PCD_PAYER_NAME"))
        .payerHp(getString(approvalResult, "PCD_PAYER_HP"))
        .payCardName(getString(approvalResult, "PCD_PAY_CARDNAME"))
        .payCardNum(getString(approvalResult, "PCD_PAY_CARDNUM"))
        .payCardQuota(getString(approvalResult, "PCD_PAY_CARDQUOTA"))
        .payCardTradeNum(getString(approvalResult, "PCD_PAY_CARDTRADENUM"))
        .payCardAuthNo(getString(approvalResult, "PCD_PAY_CARDAUTHNO"))
        .payCardReceipt(getString(approvalResult, "PCD_CARD_RECEIPT"))
        .errorCode(isSuccess ? null : getString(approvalResult, "PCD_PAY_CODE"))
        .errorMessage(isSuccess ? null : getString(approvalResult, "PCD_PAY_MSG"))
        .build();
  }

  /** 공통 인증 응답 빌더 */
  private PaypleAuthResponseDTO buildAuthResponse(JSONObject authResult, String authType) {
    String result = getString(authResult, "result");
    if (!RESULT_SUCCESS.equalsIgnoreCase(result)) {
      String errorMsg = getString(authResult, "result_msg");
      log.error("페이플 {} 실패 - message: {}", authType, errorMsg);
      throw new PaypleApiException("페이플 " + authType + " 실패: " + errorMsg);
    }

    return PaypleAuthResponseDTO.builder()
        .result(result)
        .resultMsg(getString(authResult, "result_msg"))
        .cstId(getString(authResult, "cst_id"))
        .custKey(getString(authResult, "custKey"))
        .authKey(getString(authResult, "AuthKey"))
        .payWork(getString(authResult, "PCD_PAY_WORK"))
        .payUrl(getString(authResult, "PCD_PAY_URL"))
        .returnUrl(getString(authResult, "return_url"))
        .build();
  }

  /** 코드 마스킹 (보안을 위한 로깅용) */
  private String maskCode(String code) {
    if (code == null || code.length() <= 4) {
      return "****";
    }
    return code.substring(0, 2) + "****" + code.substring(code.length() - 2);
  }

  /** JSONObject에서 안전하게 문자열 추출 */
  private String getString(JSONObject json, String key) {
    Object value = json.get(key);
    return value != null ? value.toString() : null;
  }
}
