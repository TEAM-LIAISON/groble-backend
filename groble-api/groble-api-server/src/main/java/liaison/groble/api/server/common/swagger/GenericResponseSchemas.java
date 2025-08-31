package liaison.groble.api.server.common.swagger;

import liaison.groble.api.model.payment.response.AppCardPayplePaymentResponse;
import liaison.groble.api.model.purchase.response.PurchaserContentReviewResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 모든 API에서 재사용 가능한 제네릭 GrobleResponse 스키마 정의
 *
 * <p>실제 GrobleResponse 구조를 반영하여 Swagger에서 정확한 응답 예시를 제공합니다.
 */
public final class GenericResponseSchemas {

  private GenericResponseSchemas() {}

  /** 에러 정보 스키마 */
  @Schema(description = "에러 발생 시 상세 정보")
  public static class ErrorInfo {

    @Schema(description = "에러 코드", example = "VALIDATION_ERROR")
    public String code;

    @Schema(description = "에러 메시지", example = "입력값이 올바르지 않습니다.")
    public String message;

    @Schema(description = "예외 클래스명", example = "ValidationException")
    public String exception;

    @Schema(description = "에러 발생 필드", example = "email")
    public String field;

    @Schema(description = "스택 트레이스 (개발 환경에서만 제공)")
    public String trace;
  }

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

    @Schema(description = "에러 정보 (성공 시 null)")
    public ErrorInfo error;

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

    @Schema(description = "에러 정보 (성공 시 null)")
    public ErrorInfo error;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  /** 결제 취소 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 결제 취소 응답")
  public static class PaymentCancelResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "결제 취소 요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "결제 취소 결과 데이터")
    public liaison.groble.application.payment.dto.cancel.PaymentCancelResponse data;

    @Schema(description = "에러 정보 (성공 시 null)")
    public ErrorInfo error;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }

  // 다른 도메인 응답들도 필요에 따라 여기에 추가할 수 있습니다.
  // 예: UserResponse, ContentResponse, MarketResponse 등
  /** 리뷰 작성/수정 응답 */
  @Schema(description = CommonSwaggerDocs.GROBLE_RESPONSE_DESC + " - 리뷰 작성/수정 응답")
  public static class ContentReviewRequestResponse {

    @Schema(description = "응답 상태", example = CommonSwaggerDocs.STATUS_SUCCESS)
    public String status;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int code;

    @Schema(description = "응답 메시지", example = "리뷰 작성/수정이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "리뷰 작성/수정 결과 데이터")
    public PurchaserContentReviewResponse data;

    @Schema(description = "에러 정보 (성공 시 null)")
    public ErrorInfo error;

    @Schema(description = "응답 시간", example = "2025-08-31T07:06:06.312Z")
    public String timestamp;
  }
}
