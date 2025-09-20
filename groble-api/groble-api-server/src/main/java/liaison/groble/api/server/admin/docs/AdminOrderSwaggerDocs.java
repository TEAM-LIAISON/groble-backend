package liaison.groble.api.server.admin.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

/** 관리자 주문 관리 API 명세 상수 모음 */
public final class AdminOrderSwaggerDocs {

  private AdminOrderSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Admin.ORDER;
  public static final String TAG_DESCRIPTION = SwaggerTags.Admin.ORDER_DESC;

  // === 모든 주문 목록 조회 ===
  public static final String GET_ALL_ORDERS_SUMMARY = "[✅ 관리자 주문 관리] 주문 목록 조회";
  public static final String GET_ALL_ORDERS_DESCRIPTION =
      """
          결제 완료, 취소 요청, 환불 완료 주문 등 관리자 페이지의 모든 주문을 조건 없이 조회합니다.
          \n- 페이지, 사이즈, 정렬 조건을 통해 목록을 페이징으로 응답합니다.
          """;

  // === 결제 취소 사유 조회 ===
  public static final String GET_CANCELLATION_REASON_SUMMARY = "[✅ 관리자 주문 관리] 결제 취소 사유 조회";
  public static final String GET_CANCELLATION_REASON_DESCRIPTION =
      """
          특정 주문의 결제 취소 사유를 조회합니다.
          \n- merchantUid를 기준으로 취소 사유 및 관련 메타 정보를 응답합니다.
          """;

  // === 결제 취소 요청 처리 ===
  public static final String HANDLE_CANCEL_REQUEST_SUMMARY = "[✅ 관리자 주문 관리] 결제 취소 요청 승인/거절";
  public static final String HANDLE_CANCEL_REQUEST_DESCRIPTION =
      """
          결제 취소 요청 주문을 승인하거나 거절합니다.
          \n- 승인 시 결제 취소 프로세스를 진행하고, 거절 시 사유와 함께 상태를 업데이트합니다.
          """;
}
