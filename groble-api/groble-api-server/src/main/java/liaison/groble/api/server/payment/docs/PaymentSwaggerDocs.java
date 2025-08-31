package liaison.groble.api.server.payment.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

/**
 * 결제 API Swagger 문서 상수 클래스
 *
 * <p>Controller의 가독성을 높이기 위해 Swagger 문서 내용을 별도로 관리합니다.
 */
public final class PaymentSwaggerDocs {

  private PaymentSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Payment.PAYPLE;
  public static final String TAG_DESCRIPTION = SwaggerTags.Payment.PAYPLE_DESC;

  // === 결제 요청 API ===
  public static final String PAYMENT_SUMMARY = "[✅ 통합 앱카드 결제 승인] 회원/비회원 페이플 앱카드 결제를 진행합니다.";
  public static final String PAYMENT_DESCRIPTION =
      """
            토큰 종류에 따라 회원/비회원을 자동 판단하여 앱카드 결제 인증 결과를 수신하고, Payple 서버에 승인 요청을 보냅니다.

            **주의사항:**
            - 인증 실패 시 400 에러가 발생합니다
            - 결제창이 닫힌 경우 빈 응답을 반환합니다
            - 결제 승인은 비동기로 처리되며, 완료 시 이벤트가 발행됩니다
            - 회원 로그인 또는 비회원 인증이 필요합니다
            """;

  // === 결제 취소 API ===
  public static final String CANCEL_SUMMARY = "[❌ 통합 결제 취소] 관리자가 서비스 유형에 한해 회원/비회원 결제를 취소합니다.";
  public static final String CANCEL_DESCRIPTION =
      """
            토큰 종류에 따라 회원/비회원을 자동 판단하여 완료된 서비스형 콘텐츠에 대해 결제를 취소하고 환불 처리합니다.

            **취소 가능 조건:**
            - 주문 상태가 CANCEL_REQUEST인 경우만 가능
            - 서비스(type=COACHING) 콘텐츠에 한함
            - 본인의 주문만 취소 가능
            - 회원 로그인 또는 비회원 인증이 필요합니다

            **처리 과정:**
            1. 주문 및 결제 정보 검증
            2. 페이플 환불 API 호출
            3. 성공 시 주문/결제/구매 상태 업데이트
            4. 환불 완료 이벤트 발행
            """;

  // === Parameter 설명 ===
  public static final String MERCHANT_UID_DESC = "주문번호";
  public static final String MERCHANT_UID_EXAMPLE = "20240101000001";
}
