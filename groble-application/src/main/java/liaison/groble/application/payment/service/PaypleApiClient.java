package liaison.groble.application.payment.service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import liaison.groble.application.payment.dto.PaymentCancelInfo;
import liaison.groble.application.payment.dto.PaypleApprovalResult;
import liaison.groble.application.payment.dto.PaypleAuthResponseDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.PaypleRefundResult;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.external.adapter.payment.PaypleRefundRequest;
import liaison.groble.external.adapter.payment.PaypleService;
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
  private static final String PAY_WORK_AUTH = "AUTH";
  private static final String PAY_WORK_APPCARD = "APPCARD";
  private static final String RESULT_SUCCESS = "success";
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final PaypleService paypleService;
  private final PaypleConfig paypleConfig;

  /**
   * 파트너 인증 요청
   *
   * @param payWork 작업 구분
   * @return 인증 응답
   */
  @Retryable(
      value = PaypleApiException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public PaypleAuthResponseDTO requestAuth(String payWork) {
    log.info("페이플 파트너 인증 요청 - payWork: {}", payWork);

    Map<String, String> params = new HashMap<>();
    params.put("cst_id", paypleConfig.getCstId());
    params.put("custKey", paypleConfig.getCustKey());
    params.put("PCD_PAY_WORK", payWork);

    try {
      JSONObject authResult = paypleService.payAuth(params);

      String result = getString(authResult, "result");
      if (!RESULT_SUCCESS.equalsIgnoreCase(result)) {
        String errorMsg = getString(authResult, "result_msg");
        log.error("페이플 파트너 인증 실패 - message: {}", errorMsg);
        throw new PaypleApiException("페이플 파트너 인증 실패: " + errorMsg);
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

    } catch (Exception e) {
      log.error("페이플 파트너 인증 중 오류 발생", e);
      throw new PaypleApiException("페이플 파트너 인증 실패", e);
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

    try {
      JSONObject approvalResult = paypleService.payAppCard(params);

      String payRst = getString(approvalResult, "PCD_PAY_RST");
      boolean isSuccess = RESULT_SUCCESS.equalsIgnoreCase(payRst);

      log.info("페이플 승인 응답 - merchantUid: {}, result: {}", authResult.getPayOid(), payRst);

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
          .payCardName(getString(approvalResult, "PCD_PAY_CARDNAME"))
          .payCardNum(getString(approvalResult, "PCD_PAY_CARDNUM"))
          .payCardQuota(getString(approvalResult, "PCD_PAY_CARDQUOTA"))
          .payCardTradeNum(getString(approvalResult, "PCD_PAY_CARDTRADENUM"))
          .payCardAuthNo(getString(approvalResult, "PCD_PAY_CARDAUTHNO"))
          .payCardReceipt(getString(approvalResult, "PCD_CARD_RECEIPT"))
          .errorCode(isSuccess ? null : getString(approvalResult, "PCD_PAY_CODE"))
          .errorMessage(isSuccess ? null : getString(approvalResult, "PCD_PAY_MSG"))
          .build();

    } catch (Exception e) {
      log.error("페이플 승인 요청 중 오류 발생 - merchantUid: {}", authResult.getPayOid(), e);
      throw new PaypleApiException("페이플 승인 요청 실패", e);
    }
  }

  /**
   * 환불 요청
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
      // 1. 환불을 위한 인증 요청
      PaypleAuthResponseDTO authResponse = requestAuth(PAY_WORK_AUTH);

      // 2. 환불 요청 생성
      PaypleRefundRequest refundRequest =
          PaypleRefundRequest.builder()
              .authKey(authResponse.getAuthKey())
              .payOid(cancelInfo.getMerchantUid())
              .payDate(cancelInfo.getPayDate().format(DATE_FORMAT))
              .refundTotal(cancelInfo.getRefundAmount().toString())
              .refundTaxtotal(
                  cancelInfo.getRefundTaxAmount() != null
                      ? cancelInfo.getRefundTaxAmount().toString()
                      : null)
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

  /** JSONObject에서 안전하게 문자열 추출 */
  private String getString(JSONObject json, String key) {
    Object value = json.get(key);
    return value != null ? value.toString() : null;
  }
}
