package liaison.groble.api.server.guest.docs;

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

public class GuestAuthPostResponses {
  private GuestAuthPostResponses() {}

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
                    @Schema(implementation = GenericResponseSchemas.ApiGuestAuthResponse.class))),
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
  public @interface AuthCodeRequestResponses {}

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
                        implementation = GenericResponseSchemas.ApiVerifyGuestAuthResponse.class))),
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
  public @interface VerifyAuthCodeResponses {}

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
                        implementation =
                            GenericResponseSchemas.ApiUpdateGuestUserInfoResponse.class))),
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
  public @interface UpdateGuestUserInfoResponses {}
}
