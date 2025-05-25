package liaison.groble.external.adapter.payment;

import java.util.Map;

import org.json.simple.JSONObject;

public interface PaypleService {

  /**
   * 파트너 인증
   *
   * @param params 인증 요청 파라미터
   * @return 인증 응답 정보
   */
  JSONObject payAuth(Map<String, String> params);

  /**
   * 결제 취소
   *
   * @param request 결제 취소 요청 정보
   * @return 취소 결과
   */
  JSONObject payRefund(PaypleRefundRequest request);

  /**
   * 결제 결과 확인
   *
   * @param request 결제 조회 요청 정보
   * @return 결제 정보
   */
  JSONObject payInfo(PayplePayInfoRequest request);

  /**
   * 빌링키 결제 (정기결제)
   *
   * @param request 빌링 결제 요청 정보
   * @return 결제 결과
   */
  JSONObject paySimple(PaypleSimplePayRequest request);

  /**
   * 빌링키 정보 조회
   *
   * @param payerId 결제자 ID (빌링키)
   * @return 빌링키 정보
   */
  JSONObject payUserInfo(String payerId);

  /**
   * 빌링키 해지
   *
   * @param payerId 결제자 ID (빌링키)
   * @return 해지 결과
   */
  JSONObject payUserDel(String payerId);
}
