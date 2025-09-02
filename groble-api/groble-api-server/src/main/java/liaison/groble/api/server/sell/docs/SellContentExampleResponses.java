package liaison.groble.api.server.sell.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public final class SellContentExampleResponses {

  private SellContentExampleResponses() {}

  /** 판매 관리 페이지 조회 성공 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "판매 관리 페이지 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(
                      type = "object",
                      example = SellContentExamples.SELL_MANAGE_PAGE_SUCCESS_EXAMPLE)))
  public @interface SellManagePageSuccess {}

  /** 판매 콘텐츠 리스트 조회 성공 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "판매 콘텐츠 리스트 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(
                      type = "object",
                      example = SellContentExamples.CONTENT_SELL_LIST_SUCCESS_EXAMPLE)))
  public @interface ContentSellListSuccess {}

  /** 판매 콘텐츠 상세 조회 성공 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "판매 콘텐츠 상세 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(
                      type = "object",
                      example = SellContentExamples.CONTENT_SELL_DETAIL_SUCCESS_EXAMPLE)))
  public @interface ContentSellDetailSuccess {}

  /** 리뷰 리스트 조회 성공 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "리뷰 리스트 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(
                      type = "object",
                      example = SellContentExamples.CONTENT_REVIEW_LIST_SUCCESS_EXAMPLE)))
  public @interface ContentReviewListSuccess {}

  /** 리뷰 상세 조회 성공 응답 */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "리뷰 상세 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(
                      type = "object",
                      example = SellContentExamples.CONTENT_REVIEW_DETAIL_SUCCESS_EXAMPLE)))
  public @interface ContentReviewDetailSuccess {}
}
