package liaison.groble.api.server.order.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

public class OrderSwaggerDocs {
  private OrderSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Purchase.REVIEW;
  public static final String TAG_DESCRIPTION = SwaggerTags.Purchase.REVIEW_DESC;

  // === 회원/비회원 통합 주문 생성 요청 API ===
  public static final String CREATE_ORDER_SUMMARY = "[✅ 통합 주문 발행] 회원/비회원 자동 판단 주문 발행 요청";
  public static final String CREATE_ORDER_DESCRIPTION =
      "토큰 종류에 따라 회원/비회원을 자동 판단하여 통합 주문 발행을 요청합니다. <br/>" + "- 요청 성공 시, 발행된 주문의 상세 정보를 응답합니다.";

  // === 회원/비회원 통합 주문 상세 조회 API ===
  public static final String GET_ORDER_SUMMARY = "[✅ 통합 주문 조회] 회원/비회원 주문 성공 페이지 정보 조회";
  public static final String GET_ORDER_DESCRIPTION =
      "토큰 종류에 따라 회원/비회원을 자동 판단하여 통합 주문 상세 정보를 조회합니다. <br/>" + "- 요청 성공 시, 조회된 주문의 상세 정보를 응답합니다.";
}
