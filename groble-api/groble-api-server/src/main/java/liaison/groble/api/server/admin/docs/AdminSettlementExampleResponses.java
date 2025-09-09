package liaison.groble.api.server.admin.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liaison.groble.api.server.common.swagger.CommonSwaggerDocs;
import liaison.groble.api.server.common.swagger.GenericResponseSchemas;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public final class AdminSettlementExampleResponses {
  private AdminSettlementExampleResponses() {}

  /** 관리자 전체 사용자 정산 내역 조회 성공 응답 */
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
                            GenericResponseSchemas.ApiAllUsersSettlementsResponse.class),
                examples =
                    @ExampleObject(
                        name = "성공 응답 예시",
                        description = "전체 사용자 정산 내역 조회 성공 시 응답 예시",
                        value = AdminSettlementExamples.ALL_USERS_SETTLEMENTS_SUCCESS_EXAMPLE))),
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
  public @interface AllUsersSettlementsPageSuccess {}

  /** 관리자 특정 정산 항목 상세 내역 조회 성공 응답 */
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
                            GenericResponseSchemas.ApiAdminSettlementDetailResponse.class),
                examples =
                    @ExampleObject(
                        name = "성공 응답 예시",
                        description = "정산 상세 조회 성공 시 응답 예시",
                        value = AdminSettlementExamples.ADMIN_SETTLEMENT_DETAIL_SUCCESS_EXAMPLE))),
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
  public @interface AdminSettlementDetailSuccess {}

  /** 관리자 정산 판매 내역 조회 성공 응답 */
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
                            GenericResponseSchemas.ApiAdminSettlementSalesListResponse.class),
                examples =
                    @ExampleObject(
                        name = "성공 응답 예시",
                        description = "정산 판매 내역 조회 성공 시 응답 예시",
                        value =
                            AdminSettlementExamples.ADMIN_SETTLEMENT_SALES_LIST_SUCCESS_EXAMPLE))),
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
  public @interface AdminSettlementSalesListSuccess {}
}
