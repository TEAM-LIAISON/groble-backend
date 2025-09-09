package liaison.groble.api.server.auth.docs;

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

/** 계좌 인증 API 공통 응답 어노테이션 */
public final class AccountVerificationApiResponses {

  private AccountVerificationApiResponses() {}

  /** 개인 메이커 계좌 인증 API 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = CommonSwaggerDocs.SUCCESS_200,
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GenericResponseSchemas.VoidResponse.class))),
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
        responseCode = "409",
        description = CommonSwaggerDocs.CONFLICT,
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = CommonSwaggerDocs.CONFLICT_EXAMPLE))),
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
  public @interface PersonalMakerAccountVerificationResponses {}

  /** 사업자 통장 사본 인증 API 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = CommonSwaggerDocs.SUCCESS_200,
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GenericResponseSchemas.VoidResponse.class))),
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
        responseCode = "409",
        description = CommonSwaggerDocs.CONFLICT,
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = CommonSwaggerDocs.CONFLICT_EXAMPLE))),
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
  public @interface BusinessBankbookVerificationResponses {}

  /** 사업자 계좌 인증 API 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = CommonSwaggerDocs.SUCCESS_200,
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GenericResponseSchemas.VoidResponse.class))),
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
        responseCode = "409",
        description = CommonSwaggerDocs.CONFLICT,
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = CommonSwaggerDocs.CONFLICT_EXAMPLE))),
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
  public @interface BusinessLicenseVerificationResponses {}
}
