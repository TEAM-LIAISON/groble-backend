package liaison.groble.api.server.payment.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

/** 결제 API Swagger 문서 상수 클래스 */
public final class PaymentSwaggerDocs {

  private PaymentSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Payment.PAYPLE;
  public static final String TAG_DESCRIPTION = SwaggerTags.Payment.PAYPLE_DESC;

  // === 앱카드 결제 요청 API ===
  public static final String PAYMENT_APP_CARD_SUMMARY =
      "[✅ 통합 앱카드 결제 승인] 회원/비회원 페이플 앱카드 결제를 진행합니다.";
  public static final String PAYMENT_APP_CARD_DESCRIPTION =
      """
            토큰 종류에 따라 회원/비회원을 자동 판단하여 앱카드 결제 인증 결과를 수신하고, Payple 서버에 승인 요청을 보냅니다.

            **주의사항:**
            - 인증 실패 시 400 에러가 발생합니다
            - 결제창이 닫힌 경우 빈 응답을 반환합니다
            - 결제 승인은 비동기로 처리되며, 완료 시 이벤트가 발행됩니다
            - 회원 로그인 또는 비회원 인증이 필요합니다
            """;

  // === 정기(빌링) 결제 요청 API ===
  public static final String PAYMENT_BILLING_SUMMARY =
      "[✅ 통합 정기(빌링) 결제 승인] 회원 페이플 정기(빌링) 결제를 진행합니다.";
  public static final String PAYMENT_BILLING_DESCRIPTION =
      """
            회원 빌링키를 기반으로 페이플 서버에 정기(빌링) 결제 승인 요청을 보냅니다.

            **주의사항:**
            - 정기(빌링) 결제는 회원만 이용 가능합니다
            - 결제 승인은 비동기로 처리되며, 완료 시 이벤트가 발행됩니다
            - 회원 로그인이 필요합니다
            - 빌링키는 별도 API 통해 발급받아야 합니다
            - 매달 자동 결제되는 구독형 콘텐츠에 한해 결제가 진행됩니다
            """;

  // === 빌링 파트너 인증 API ===
  public static final String BILLING_AUTH_MO_SUMMARY =
      "[✅ 빌링 파트너 인증 - MO] 페이플 빌링 등록(MO) 결제창 호출 정보를 발급합니다.";
  public static final String BILLING_AUTH_MO_DESCRIPTION =
      """
            페이플 MO(redirect) 방식 빌링 결제창을 호출하기 위한 인증 정보를 제공합니다.

            **주요 응답 값**
            - `authKey`: 이후 결제 승인에 사용하는 인증 키
            - `payUrl`: MO 방식 결제창 URL
            - `returnUrl`: 결제 완료 후 리다이렉트 URL
            """;

  public static final String BILLING_AUTH_API_SUMMARY =
      "[✅ 빌링 파트너 인증 - API] 페이플 빌링 API 호출용 인증 정보를 발급합니다.";
  public static final String BILLING_AUTH_API_DESCRIPTION =
      """
            페이플 API(절대경로) 방식으로 빌링 결제를 요청할 때 필요한 인증 정보를 제공합니다.

            **주요 응답 값**
            - `authKey`: 빌링 결제 승인 시 사용할 인증 키
            - `custKey`: 서버 간 통신 시 필요한 파트너 키
            """;

  // === 빌링키 등록 API ===
  public static final String BILLING_REGISTER_SUMMARY =
      "[💳 빌링키 등록] 정기 결제를 위한 회원 카드 정보를 등록하고 빌링키를 저장합니다.";
  public static final String BILLING_REGISTER_DESCRIPTION =
      """
            페이플 빌링 등록 완료 콜백으로 전달되는 정보를 받아 회원의 빌링키를 저장합니다.

            **주의사항:**
            - 회원 전용 API 입니다 (비회원/미로그인 불가)
            - 동일 회원이 다시 등록하면 기존 빌링키는 비활성화됩니다
            - 결제 승인 전 반드시 선행되어야 합니다
            """;

  // === 앱카드 결제 취소 API ===
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
