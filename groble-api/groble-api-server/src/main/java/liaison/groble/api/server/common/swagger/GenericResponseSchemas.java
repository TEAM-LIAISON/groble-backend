package liaison.groble.api.server.common.swagger;

import liaison.groble.api.model.admin.dashboard.response.AdminActiveVisitorsResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardOverviewResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardTopContentsResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardTrendResponse;
import liaison.groble.api.model.purchase.response.PurchaserContentReviewResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.api.server.common.ResponseMessages;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공통으로 사용되는 제네릭 GrobleResponse 스키마 정의
 *
 * <p>도메인별 스키마는 별도 클래스로 분리되었습니다: - AdminResponseSchemas: 관리자 관련 스키마 - PaymentResponseSchemas: 결제 관련
 * 스키마 - AuthResponseSchemas: 인증 관련 스키마
 */
public final class GenericResponseSchemas {

  private GenericResponseSchemas() {}

  /** 데이터가 없는 성공 응답 (Void) */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 데이터 없는 성공 응답")
  public static class VoidResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "응답 데이터 (null)")
    public Object data = null;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  // === 콘텐츠 관련 응답 스키마 ===
  /** 리뷰 작성/수정 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 리뷰 작성/수정 응답")
  public static class ApiContentReviewRequestResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "리뷰 작성/수정이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "리뷰 작성/수정 결과 데이터")
    public PurchaserContentReviewResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  /** 리뷰 답글 작성/수정 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 리뷰 답글 작성/수정 응답")
  public static class ApiReplyContentResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "리뷰 답글 작성/수정이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "리뷰 답글 데이터")
    public ReplyContentResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 관리자 대시보드 개요 응답")
  public static class ApiAdminDashboardOverviewResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = ResponseMessages.Admin.DASHBOARD_OVERVIEW_RETRIEVED)
    public String message;

    @Schema(description = "관리자 대시보드 개요 데이터")
    public AdminDashboardOverviewResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 관리자 대시보드 추세 응답")
  public static class ApiAdminDashboardTrendResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = ResponseMessages.Admin.DASHBOARD_TRENDS_RETRIEVED)
    public String message;

    @Schema(description = "관리자 대시보드 일자별 추세 데이터")
    public AdminDashboardTrendResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 관리자 대시보드 인기 콘텐츠 응답")
  public static class ApiAdminDashboardTopContentsResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(
        description = "응답 메시지",
        example = ResponseMessages.Admin.DASHBOARD_TOP_CONTENTS_RETRIEVED)
    public String message;

    @Schema(description = "관리자 대시보드 인기 콘텐츠 리스트")
    public AdminDashboardTopContentsResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 관리자 대시보드 실시간 방문자 응답")
  public static class ApiAdminActiveVisitorsResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(
        description = "응답 메시지",
        example = ResponseMessages.Admin.DASHBOARD_ACTIVE_VISITORS_RETRIEVED)
    public String message;

    @Schema(description = "실시간 방문자 데이터")
    public AdminActiveVisitorsResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }
}
