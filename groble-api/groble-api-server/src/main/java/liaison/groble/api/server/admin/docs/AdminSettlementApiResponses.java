package liaison.groble.api.server.admin.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liaison.groble.api.server.common.swagger.AdminResponseSchemas;
import liaison.groble.api.server.common.swagger.CommonSwaggerDocs;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public final class AdminSettlementApiResponses {
  private AdminSettlementApiResponses() {}

  /** 정산 승인 성공 응답 */
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
                    @Schema(
                        implementation = AdminResponseSchemas.ApiSettlementApprovalResponse.class),
                examples =
                    @ExampleObject(
                        name = "성공 응답 예시",
                        description = "정산 승인 성공 시 응답 예시",
                        value = AdminSettlementApiExamples.SETTLEMENT_APPROVAL_SUCCESS_EXAMPLE))),
    @ApiResponse(
        responseCode = "400",
        description = CommonSwaggerDocs.BAD_REQUEST,
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = CommonSwaggerDocs.BAD_REQUEST_EXAMPLE))),
    @ApiResponse(
        responseCode = "403",
        description = CommonSwaggerDocs.FORBIDDEN,
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = CommonSwaggerDocs.FORBIDDEN_EXAMPLE))),
    @ApiResponse(
        responseCode = "404",
        description = CommonSwaggerDocs.NOT_FOUND,
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = CommonSwaggerDocs.NOT_FOUND_EXAMPLE))),
    @ApiResponse(
        responseCode = "500",
        description = CommonSwaggerDocs.SERVER_ERROR,
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = CommonSwaggerDocs.SERVER_ERROR_EXAMPLE)))
  })
  public @interface SettlementApprovalApiResponses {}
}
