package liaison.groble.api.server.sell;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.request.draft.ContentDraftRequest;
import liaison.groble.api.model.content.request.register.ContentRegisterRequest;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.ContentResponse;
import liaison.groble.api.model.content.response.ContentStatusResponse;
import liaison.groble.api.model.content.response.swagger.ContentExamineReject;
import liaison.groble.api.model.content.response.swagger.ContentListResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.service.ContentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.content.ContentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sell/content")
@RequiredArgsConstructor
@Tag(
    name = "[🎁 상품 관리] 상품 관리 단일 페이지 기능",
    description = "나의 판매중, 작성중 콘텐츠 조회를 포함한 상품 관리 단일 페이지 기능을 제공합니다.")
public class SellController {

  // API 경로 상수화
  private static final String DRAFT_CONTENT_PATH = "/draft";
  private static final String REGISTER_CONTENT_PATH = "/register";
  private static final String STOP_CONTENT_PATH = "/{contentId}/stop";
  private static final String PAUSE_SUBSCRIPTION_SALE_PATH = "/{contentId}/subscription/pause";
  private static final String TERMINATE_SUBSCRIPTION_SALE_PATH =
      "/{contentId}/subscription/terminate";
  private static final String DELETE_CONTENT_PATH = "/{contentId}/delete";
  private static final String EXAMINE_REJECT_REASON_PATH = "/{contentId}/examine/reject";
  private static final String MY_SELLING_CONTENTS_PATH = "/my/selling-contents";
  private static final String CONVERT_TO_SALE_PATH = "/{contentId}/convert-to-sale";

  // 응답 메시지 상수화
  private static final String MY_SELLING_CONTENTS_SUCCESS_MESSAGE = "나의 판매 콘텐츠 조회에 성공하였습니다.";
  private static final String CONTENT_DRAFT_SUCCESS_MESSAGE = "콘텐츠 임시 저장에 성공하였습니다.";
  private static final String CONTENT_REGISTER_SUCCESS_MESSAGE = "콘텐츠 판매하기에 성공하였습니다.";
  private static final String STOP_CONTENT_SUCCESS_MESSAGE = "콘텐츠 판매 중단에 성공하였습니다.";
  private static final String PAUSE_SUBSCRIPTION_SALE_SUCCESS_MESSAGE = "정기 결제 신규 판매 중단에 성공하였습니다.";
  private static final String TERMINATE_SUBSCRIPTION_SALE_SUCCESS_MESSAGE = "정기 결제 종료에 성공하였습니다.";
  private static final String DELETE_CONTENT_SUCCESS_MESSAGE = "콘텐츠 삭제에 성공하였습니다.";
  private static final String EXAMINE_REJECT_REASON_SUCCESS_MESSAGE = "콘텐츠 심사 거절 사유 조회에 성공하였습니다.";
  private static final String CONVERT_TO_SALE_SUCCESS_MESSAGE = "콘텐츠 판매하기 전환에 성공하였습니다.";

  // Service
  private final ContentService contentService;

  // Mapper
  private final ContentMapper contentMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @RequireRole("ROLE_SELLER")
  @Operation(summary = "[✅ 콘텐츠 임시 저장] 콘텐츠를 작성하다가 임시 저장.", description = "콘텐츠를 임시 저장합니다.")
  @Logging(item = "Sell", action = "draftContent", includeParam = true, includeResult = true)
  @PostMapping(DRAFT_CONTENT_PATH)
  public ResponseEntity<GrobleResponse<ContentResponse>> draftContent(
      @Auth Accessor accessor, @Valid @RequestBody ContentDraftRequest request) {

    ContentDTO contentDTO = contentMapper.toContentDTO(request);
    ContentDTO savedContentDTO = contentService.draftContent(accessor.getUserId(), contentDTO);

    ContentResponse response = contentMapper.toContentResponse(savedContentDTO);
    return responseHelper.success(response, CONTENT_DRAFT_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 콘텐츠 심사 요청
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[✅ 콘텐츠 심사 요청] 작성 완료한 콘텐츠 판매하기",
      description = "콘텐츠 심사를 요청합니다. 콘텐츠 유형(코칭/문서)에 따라 옵션 구조가 달라집니다.")
  @Logging(item = "Sell", action = "registerContent", includeParam = true, includeResult = true)
  @PostMapping(REGISTER_CONTENT_PATH)
  public ResponseEntity<GrobleResponse<ContentResponse>> registerContent(
      @Auth Accessor accessor, @Valid @RequestBody ContentRegisterRequest request) {
    ContentDTO contentDTO = contentMapper.toContentDTO(request);
    ContentDTO savedContentDTO = contentService.registerContent(accessor.getUserId(), contentDTO);
    ContentResponse response = contentMapper.toContentResponse(savedContentDTO);
    return responseHelper.success(response, CONTENT_REGISTER_SUCCESS_MESSAGE, HttpStatus.CREATED);
  }

  @Operation(
      summary = "[✅ 콘텐츠 심사 요청] 콘텐츠 판매 중단",
      description = "상품 관리 탭에서 판매중인 콘텐츠를 판매 중단합니다. (콘텐츠가 작성중 탭으로 이동합니다.)")
  @RequireRole("ROLE_SELLER")
  @PostMapping(STOP_CONTENT_PATH)
  public ResponseEntity<GrobleResponse<ContentStatusResponse>> stopContent(
      @Auth Accessor accessor, @PathVariable("contentId") Long contentId) {
    ContentDTO contentDTO = contentService.stopContent(accessor.getUserId(), contentId);
    ContentStatusResponse response = contentMapper.toContentStatusResponse(contentDTO);
    return responseHelper.success(response, STOP_CONTENT_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 정기 결제 판매 정책] 정기 결제 신규 판매 중단",
      description = "정기 결제 콘텐츠의 신규 구독을 중단합니다. 기존 구독자는 계속 결제됩니다.")
  @RequireRole("ROLE_SELLER")
  @Logging(
      item = "Sell",
      action = "pauseSubscriptionSale",
      includeParam = true,
      includeResult = true)
  @PostMapping(PAUSE_SUBSCRIPTION_SALE_PATH)
  public ResponseEntity<GrobleResponse<ContentStatusResponse>> pauseSubscriptionSale(
      @Auth Accessor accessor, @PathVariable("contentId") Long contentId) {

    ContentDTO contentDTO = contentService.pauseSubscriptionSale(accessor.getUserId(), contentId);
    ContentStatusResponse response = contentMapper.toContentStatusResponse(contentDTO);
    return responseHelper.success(response, PAUSE_SUBSCRIPTION_SALE_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 정기 결제 판매 정책] 정기 결제 종료",
      description = "정기 결제 콘텐츠의 모든 구독을 종료합니다. 기존 구독자는 결제된 기간까지만 이용할 수 있습니다.")
  @RequireRole("ROLE_SELLER")
  @Logging(
      item = "Sell",
      action = "terminateSubscriptionSale",
      includeParam = true,
      includeResult = true)
  @PostMapping(TERMINATE_SUBSCRIPTION_SALE_PATH)
  public ResponseEntity<GrobleResponse<ContentStatusResponse>> terminateSubscriptionSale(
      @Auth Accessor accessor, @PathVariable("contentId") Long contentId) {

    ContentDTO contentDTO =
        contentService.terminateSubscriptionSale(accessor.getUserId(), contentId);
    ContentStatusResponse response = contentMapper.toContentStatusResponse(contentDTO);
    return responseHelper.success(
        response, TERMINATE_SUBSCRIPTION_SALE_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 콘텐츠 삭제 요청] 콘텐츠 삭제",
      description = "작성 중인 콘텐츠를 삭제합니다. 판매 중단된 콘텐츠는 삭제할 수 없습니다.")
  @RequireRole("ROLE_SELLER")
  @PostMapping(DELETE_CONTENT_PATH)
  public ResponseEntity<GrobleResponse<Void>> deleteContent(
      @Auth Accessor accessor, @PathVariable("contentId") Long contentId) {
    contentService.deleteContent(accessor.getUserId(), contentId);
    return responseHelper.success(null, DELETE_CONTENT_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 심사 거절된 콘텐츠의 거절 사유를 조회
  @ContentExamineReject
  @RequireRole("ROLE_SELLER")
  @GetMapping(EXAMINE_REJECT_REASON_PATH)
  public ResponseEntity<GrobleResponse<String>> getExamineRejectReason(
      @Auth Accessor accessor, @PathVariable("contentId") Long contentId) {
    String rejectReason = contentService.getExamineRejectReason(accessor.getUserId(), contentId);
    return responseHelper.success(
        rejectReason, EXAMINE_REJECT_REASON_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 상품 관리] 상품 관리 화면에서 판매중, 작성중인 콘텐츠를 조회합니다.",
      description = "상품 관리 화면에서 판매중, 작성중인 콘텐츠를 조회합니다. 커서 기반 페이지네이션으로 작동합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "상품 관리 화면에서 콘텐츠 목록 전체 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentListResponse.class)))
  @GetMapping(MY_SELLING_CONTENTS_PATH)
  public ResponseEntity<GrobleResponse<PageResponse<ContentPreviewCardResponse>>>
      getMySellingContents(
          @Auth Accessor accessor,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "12") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
          @RequestParam(value = "state") String state) {

    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<ContentCardDTO> dtoPageResponse =
        contentService.getMySellingContents(accessor.getUserId(), pageable, state);

    PageResponse<ContentPreviewCardResponse> responsePage =
        contentMapper.toContentPreviewCardResponsePage(dtoPageResponse);

    return responseHelper.success(responsePage, MY_SELLING_CONTENTS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 상품 관리] 콘텐츠 판매하기 전환",
      description = "작성중인 콘텐츠를 판매하기로 전환합니다. (판매중인 콘텐츠로 이동합니다.)")
  @Logging(item = "Content", action = "convertToSale", includeParam = true)
  @PostMapping(CONVERT_TO_SALE_PATH)
  public ResponseEntity<GrobleResponse<Void>> convertToSale(
      @Auth Accessor accessor, @PathVariable("contentId") Long contentId) {
    contentService.convertToSale(accessor.getUserId(), contentId);
    return responseHelper.success(null, CONVERT_TO_SALE_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
