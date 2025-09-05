package liaison.groble.api.server.common.swagger;

import liaison.groble.api.model.admin.dashboard.response.AdminDashboardOverviewResponse;
import liaison.groble.api.model.admin.settlement.response.AdminSettlementsOverviewResponse;
import liaison.groble.api.model.guest.response.GuestAuthCodeResponse;
import liaison.groble.api.model.guest.response.UpdateGuestUserInfoResponse;
import liaison.groble.api.model.guest.response.VerifyAuthCodeResponse;
import liaison.groble.api.model.payment.response.AppCardPayplePaymentResponse;
import liaison.groble.api.model.purchase.response.PurchaserContentReviewResponse;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.api.model.sell.response.SellManagePageResponse;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 모든 API에서 재사용 가능한 제네릭 GrobleResponse 스키마 정의
 *
 * <p>실제 GrobleResponse 구조를 반영하여 Swagger에서 정확한 응답 예시를 제공합니다.
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

  // === 결제 관련 응답 스키마 ===

  /** 결제 승인 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 결제 승인 응답")
  public static class PaymentRequestResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "결제 승인 요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "결제 승인 결과 데이터")
    public AppCardPayplePaymentResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  /** 결제 취소 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 결제 취소 응답")
  public static class ApiPaymentCancelResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "결제 취소 요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "결제 취소 결과 데이터")
    public PaymentCancelResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  // 다른 도메인 응답들도 필요에 따라 여기에 추가할 수 있습니다.
  // 예: UserResponse, ContentResponse, MarketResponse 등
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

  // === 판매 관련 응답 스키마 ===

  /** 판매 관리 페이지 조회 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 판매 관리 페이지 조회 응답")
  public static class ApiSellManagePageResponse {

    @Schema(description = "응답 상태", example = "SUCCESS")
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "판매 관리 페이지 조회가 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "판매 관리 페이지 데이터")
    public SellManagePageResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  /** 판매 콘텐츠 리스트 조회 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 판매 콘텐츠 리스트 조회 응답")
  public static class ApiContentSellListResponse {

    @Schema(description = "응답 상태", example = "SUCCESS")
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "판매 콘텐츠 리스트 조회가 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "판매 콘텐츠 리스트 데이터")
    public PageResponse<ContentSellDetailResponse> data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  /** 판매 콘텐츠 상세 조회 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 판매 콘텐츠 상세 조회 응답")
  public static class ApiContentSellDetailResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "판매 콘텐츠 상세 조회가 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "판매 콘텐츠 상세 데이터")
    public ContentSellDetailResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  /** 리뷰 리스트 조회 응답 */
  @Schema(
      description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 리뷰 리스트 조회 응답",
      example =
          """
         {
           "status": "SUCCESS",
           "code": 200,
           "message": "리뷰 리스트 조회가 성공적으로 처리되었습니다.",
           "data": {
             "items": [
               {
                 "reviewId": 100,
                 "reviewStatus": "ACTIVE",
                 "contentTitle": "자바 프로그래밍 코칭",
                 "createdAt": "2025-04-20 10:15:30",
                 "reviewerNickname": "뚜비뚜비",
                 "reviewContent": "마음에 들어요",
                 "selectedOptionName": "옵션 이름",
                 "rating": 4.5,
                 "reviewReplies": []
               }
             ],
             "pageInfo": {
               "currentPage": 0,
               "totalPages": 10,
               "pageSize": 12,
               "totalElements": 120,
               "first": true,
               "last": false,
               "empty": false
             }
           },
           "timestamp": "2025-08-31T07:06:06.312Z"
         }
         """)
  public static class ApiContentReviewListResponse {

    @Schema(description = "응답 상태", example = "SUCCESS")
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "리뷰 리스트 조회가 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "리뷰 리스트 데이터")
    public PageResponse<ContentReviewDetailResponse> data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  /** 리뷰 상세 조회 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 리뷰 상세 조회 응답")
  public static class ApiContentReviewDetailResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "리뷰 상세 조회가 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "리뷰 상세 데이터")
    public ContentReviewDetailResponse data;

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

  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 비회원 인증 요청 응답")
  public static class ApiGuestAuthResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(
        description = "응답 메시지",
        example = ResponseMessages.Guest.GUEST_AUTH_PHONE_REQUEST_SUCCESS)
    public String message;

    @Schema(description = "비회원 인증 코드 발송 요청 응답")
    public GuestAuthCodeResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 비회원 인증 요청 응답")
  public static class ApiVerifyGuestAuthResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(
        description = "응답 메시지",
        example = ResponseMessages.Guest.VERIFY_GUEST_AUTH_PHONE_SUCCESS)
    public String message;

    @Schema(description = "비회원 인증 코드 검증 결과 응답")
    public VerifyAuthCodeResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 비회원 사용자 정보 업데이트 응답")
  public static class ApiUpdateGuestUserInfoResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = ResponseMessages.Guest.UPDATE_GUEST_USER_INFO_SUCCESS)
    public String message;

    @Schema(description = "비회원 사용자 정보 업데이트 및 정식 토큰 발급 응답")
    public UpdateGuestUserInfoResponse data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

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

  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 관리자 전체 사용자 정산 내역 조회 응답")
  public static class ApiAllUsersSettlementsResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(
        description = "응답 메시지",
        example = ResponseMessages.Admin.ALL_USERS_SETTLEMENTS_RETRIEVED)
    public String message;

    @Schema(description = "전체 사용자 정산 내역 데이터 (페이징)")
    public PageResponse<AdminSettlementsOverviewResponse> data;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }
}
