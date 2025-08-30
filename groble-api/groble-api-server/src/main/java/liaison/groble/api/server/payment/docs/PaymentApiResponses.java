package liaison.groble.api.server.payment.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/** 결제 API 공통 응답 어노테이션 */
public final class PaymentApiResponses {

  private PaymentApiResponses() {}

  /** 결제 요청 API 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = PaymentSwaggerDocs.SUCCESS_200,
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppCardPayplePaymentResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = PaymentSwaggerDocs.BAD_REQUEST_400,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = PaymentSwaggerDocs.FORBIDDEN_403,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = PaymentSwaggerDocs.CONFLICT_409,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = PaymentSwaggerDocs.SERVER_ERROR_500,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class)))
  })
  public @interface PaymentRequestResponses {}

  /** 결제 취소 API 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = PaymentSwaggerDocs.SUCCESS_200,
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentCancelResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = PaymentSwaggerDocs.BAD_REQUEST_400,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = PaymentSwaggerDocs.FORBIDDEN_403,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = PaymentSwaggerDocs.NOT_FOUND_404,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = PaymentSwaggerDocs.CONFLICT_409,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = PaymentSwaggerDocs.SERVER_ERROR_500,
        content = @Content(schema = @Schema(implementation = GrobleResponse.class)))
  })
  public @interface PaymentCancelResponses {}
}
