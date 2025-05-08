package liaison.groble.api.server.content;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.request.draft.ContentDraftRequest;
import liaison.groble.api.model.content.request.register.ContentRegisterRequest;
import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.ContentResponse;
import liaison.groble.api.model.content.response.ContentStatusResponse;
import liaison.groble.api.model.content.response.swagger.ContentDetail;
import liaison.groble.api.model.content.response.swagger.ContentDraft;
import liaison.groble.api.model.content.response.swagger.ContentRegister;
import liaison.groble.api.model.content.response.swagger.MySellingContents;
import liaison.groble.api.server.content.mapper.ContentDtoMapper;
import liaison.groble.application.content.ContentService;
import liaison.groble.application.content.dto.ContentCardDto;
import liaison.groble.application.content.dto.ContentDetailDto;
import liaison.groble.application.content.dto.ContentDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.request.CursorRequest;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/contents")
@Tag(name = "콘텐츠 API", description = "콘텐츠 조회 및 등록(임시 저장, 심사 요청) API")
public class ContentController {

  private final ContentService contentService;
  private final ContentDtoMapper contentDtoMapper;

  public ContentController(ContentService contentService, ContentDtoMapper contentDtoMapper) {
    this.contentService = contentService;
    this.contentDtoMapper = contentDtoMapper;
  }

  // 콘텐츠 상세 조회
  @ContentDetail
  @GetMapping("/{contentId}")
  public ResponseEntity<GrobleResponse<ContentDetailResponse>> getContentDetail(
      @PathVariable("contentId") Long contentId) {
    ContentDetailDto contentDetailDto = contentService.getContentDetail(contentId);
    ContentDetailResponse response = contentDtoMapper.toContentDetailResponse(contentDetailDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "콘텐츠 상세 조회 성공"));
  }

  // 콘텐츠 임시 저장
  @ContentDraft
  @PostMapping("/draft")
  public ResponseEntity<GrobleResponse<ContentResponse>> saveDraft(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @Valid @RequestBody ContentDraftRequest request) {

    ContentDto contentDto = contentDtoMapper.toServiceContentDtoFromDraft(request);
    ContentDto savedContentDto =
        contentService.saveDraftAndReturn(accessor.getUserId(), contentDto);

    ContentResponse response = contentDtoMapper.toContentDraftResponse(savedContentDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "콘텐츠 임시 저장 성공"));
  }

  // 콘텐츠 심사 요청
  @ContentRegister
  @PostMapping("/register")
  public ResponseEntity<GrobleResponse<ContentResponse>> registerContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @Valid @RequestBody ContentRegisterRequest request) {
    ContentDto contentDto = contentDtoMapper.toServiceContentDtoFromRegister(request);
    ContentDto savedContentDto = contentService.registerContent(accessor.getUserId(), contentDto);
    ContentResponse response = contentDtoMapper.toContentDraftResponse(savedContentDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "콘텐츠 심사 요청 성공"));
  }

  // 콘텐츠 심사 완료(승인) 이후 콘텐츠 판매중으로 활성화
  @Operation(summary = "콘텐츠 활성화", description = "심사완료된 콘텐츠를 활성화합니다.")
  @PostMapping("/{contentId}/active")
  public ContentStatusResponse activateContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId) {
    ContentDto contentDto = contentService.activateContent(accessor.getUserId(), contentId);
    return contentDtoMapper.toContentStatusResponse(contentDto);
  }

  @MySellingContents
  @GetMapping("/my/selling-contents")
  public ResponseEntity<GrobleResponse<CursorResponse<ContentPreviewCardResponse>>>
      getMySellingContents(
          @Parameter(hidden = true) @Auth Accessor accessor,
          @Parameter(
                  description = "커서 기반 페이지네이션 요청 정보",
                  required = true,
                  schema = @Schema(implementation = CursorRequest.class))
              @Valid
              @ModelAttribute
              CursorRequest cursorRequest,
          @Parameter(
                  description =
                      "콘텐츠 상태 필터 [ACTIVE - 판매중], [DRAFT - 작성중], [PENDING - 심사중], [APPROVED - 심사완료]",
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"ACTIVE", "DRAFT", "PENDING", "APPROVED"}))
              @RequestParam(value = "state")
              String state,
          @Parameter(
                  description = "콘텐츠 유형 [COACHING - 코창], [DOCUMENT - 자료]",
                  required = true,
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"COACHING", "DOCUMENT"}))
              @RequestParam(value = "type")
              String type) {

    CursorResponse<ContentCardDto> cardDtos =
        contentService.getMySellingContents(
            accessor.getUserId(), cursorRequest.getCursor(), cursorRequest.getSize(), state, type);

    // DTO 변환
    List<ContentPreviewCardResponse> responseItems =
        cardDtos.getItems().stream()
            .map(contentDtoMapper::toContentPreviewCardFromCardDto)
            .toList();

    // CursorResponse 생성
    CursorResponse<ContentPreviewCardResponse> response =
        CursorResponse.<ContentPreviewCardResponse>builder()
            .items(responseItems)
            .nextCursor(cardDtos.getNextCursor())
            .hasNext(cardDtos.isHasNext())
            .totalCount(cardDtos.getTotalCount())
            .meta(cardDtos.getMeta())
            .build();

    String successMessage = "COACHING".equals(type) ? "나의 코칭 콘텐츠 조회 성공" : "나의 자료 콘텐츠 조회 성공";
    return ResponseEntity.ok(GrobleResponse.success(response, successMessage));
  }

  @Operation(summary = "홈화면 콘텐츠 조회", description = "홈화면 콘텐츠를 조회합니다.")
  @GetMapping("/home")
  public ResponseEntity<GrobleResponse<CursorResponse<ContentPreviewCardResponse>>> getHomeContents(
      @Parameter(
              description = "커서 기반 페이지네이션 요청 정보",
              required = true,
              schema = @Schema(implementation = CursorRequest.class))
          @Valid
          @ModelAttribute
          CursorRequest cursorRequest,
      @Parameter(
              description = "콘텐츠 타입 (COACHING 또는 DOCUMENT)",
              required = true,
              schema =
                  @Schema(
                      implementation = String.class,
                      allowableValues = {"COACHING", "DOCUMENT"}))
          @RequestParam(value = "type")
          String type) {
    CursorResponse<ContentCardDto> cardDtos =
        contentService.getHomeContents(cursorRequest.getCursor(), cursorRequest.getSize(), type);

    // DTO 변환
    List<ContentPreviewCardResponse> responseItems =
        cardDtos.getItems().stream()
            .map(contentDtoMapper::toContentPreviewCardFromCardDto)
            .toList();

    // CursorResponse 생성
    CursorResponse<ContentPreviewCardResponse> response =
        CursorResponse.<ContentPreviewCardResponse>builder()
            .items(responseItems)
            .nextCursor(cardDtos.getNextCursor())
            .hasNext(cardDtos.isHasNext())
            .totalCount(cardDtos.getTotalCount())
            .meta(cardDtos.getMeta())
            .build();

    String successMessage = "COACHING".equals(type) ? "홈화면 코칭 콘텐츠 조회 성공" : "홈화면 자료 콘텐츠 조회 성공";
    return ResponseEntity.ok(GrobleResponse.success(response, successMessage));
  }

  // 콘텐츠 심사 [반려]
  @Operation(summary = "콘텐츠 심사 반려 [관리자 기능]", description = "콘텐츠 심사를 반려합니다. [관리자 기능]")
  @PostMapping("/{contentId}/reject")
  public ResponseEntity<GrobleResponse<Void>> rejectContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId) {
    contentService.rejectContent(accessor.getUserId(), contentId);
    return ResponseEntity.ok(GrobleResponse.success(null, "콘텐츠 심사 반려 성공"));
  }
}
