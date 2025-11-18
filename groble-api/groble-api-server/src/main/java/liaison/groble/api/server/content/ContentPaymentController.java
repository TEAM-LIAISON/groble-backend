package liaison.groble.api.server.content;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.response.pay.ContentPayPageResponse;
import liaison.groble.application.content.dto.ContentPayPageDTO;
import liaison.groble.application.content.service.ContentPaymentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.content.ContentPaymentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "[💰 콘텐츠 결제 페이지 조회]", description = "콘텐츠 결제 페이지 정보를 조회하는 API")
public class ContentPaymentController {

  // API 경로 상수화
  private static final String CONTENT_PAYMENT_PAGE_PATH = "/content/{contentId}/pay/{optionId}";

  // 응답 메시지 상수화
  private static final String CONTENT_PAYMENT_PAGE_RESPONSE_MESSAGE =
      "콘텐츠 결제 페이지 정보를 조회하는 데 성공했습니다.";

  // Service
  private final ContentPaymentService contentPaymentService;

  // Mapper
  private final ContentPaymentMapper contentPaymentMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(summary = "[✅ 콘텐츠 결제 페이지 정보 조회]")
  @ApiResponse(
      responseCode = "200",
      description = CONTENT_PAYMENT_PAGE_RESPONSE_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentPayPageResponse.class)))
  @GetMapping(CONTENT_PAYMENT_PAGE_PATH)
  public ResponseEntity<GrobleResponse<ContentPayPageResponse>> getContentPayPage(
      @Auth(required = false) Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @PathVariable("optionId") Long optionId) {
    // 기본 컨텐츠 결제 페이지 정보 조회
    Long userId = accessor != null ? accessor.getUserId() : null;
    ContentPayPageDTO contentPayPageDTO =
        contentPaymentService.getContentPayPage(userId, contentId, optionId);

    ContentPayPageResponse contentPayPageResponse =
        contentPaymentMapper.toContentPayPageResponse(contentPayPageDTO);

    return responseHelper.success(
        contentPayPageResponse, CONTENT_PAYMENT_PAGE_RESPONSE_MESSAGE, HttpStatus.OK);
  }
}
