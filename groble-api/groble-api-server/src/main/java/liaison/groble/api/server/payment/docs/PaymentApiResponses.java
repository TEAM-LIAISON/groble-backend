package liaison.groble.api.server.payment.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liaison.groble.api.server.common.swagger.CommonSwaggerDocs;
import liaison.groble.api.server.common.swagger.GenericResponseSchemas;

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
        description = CommonSwaggerDocs.SUCCESS_200,
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = GenericResponseSchemas.PaymentRequestResponse.class))),
    @ApiResponse(responseCode = "400", description = CommonSwaggerDocs.BAD_REQUEST),
    @ApiResponse(responseCode = "403", description = CommonSwaggerDocs.FORBIDDEN),
    @ApiResponse(responseCode = "404", description = CommonSwaggerDocs.NOT_FOUND),
    @ApiResponse(responseCode = "409", description = CommonSwaggerDocs.CONFLICT),
    @ApiResponse(responseCode = "500", description = CommonSwaggerDocs.SERVER_ERROR)
  })
  public @interface PaymentRequestResponses {}

  /** 결제 취소 API 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = CommonSwaggerDocs.SUCCESS_200,
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = GenericResponseSchemas.PaymentCancelResponse.class))),
    @ApiResponse(responseCode = "400", description = CommonSwaggerDocs.BAD_REQUEST),
    @ApiResponse(responseCode = "403", description = CommonSwaggerDocs.FORBIDDEN),
    @ApiResponse(responseCode = "404", description = CommonSwaggerDocs.NOT_FOUND),
    @ApiResponse(responseCode = "409", description = CommonSwaggerDocs.CONFLICT),
    @ApiResponse(responseCode = "500", description = CommonSwaggerDocs.SERVER_ERROR)
  })
  public @interface PaymentCancelResponses {}
}
