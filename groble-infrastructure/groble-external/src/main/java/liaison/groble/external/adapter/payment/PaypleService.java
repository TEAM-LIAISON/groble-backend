package liaison.groble.external.adapter.payment;

import java.util.Map;

import org.json.simple.JSONObject;

public interface PaypleService {

  JSONObject payAppCard(Map<String, String> params);

  JSONObject payConfirm(Map<String, String> params);

  /** 일반 결제 인증 (기존 호환성 유지) */
  JSONObject payAuth(Map<String, String> params);

  /** 결제 취소를 위한 전용 인증 필요한 파라미터: cst_id, custKey, PCD_PAYCANCEL_FLAG */
  JSONObject payAuthForCancel();

  /** 정산지급대행을 위한 전용 인증 필요한 파라미터: cst_id, custKey, code (영문+숫자 10자리) */
  JSONObject payAuthForSettlement(String code);

  // 환불(결제 취소)
  JSONObject payRefund(PaypleRefundRequest request);

  JSONObject paySimplePayment(Map<String, String> params);

  /** 정산지급대행 계좌 검증 API */
  JSONObject payAccountVerification(Map<String, String> params);
}
