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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/contents")
@Tag(name = "컨텐츠 API", description = "컨텐츠 조회 및 등록(임시 저장, 심사 요청) API")
public class ContentController {

  private final ContentService contentService;
  private final ContentDtoMapper contentDtoMapper;

  public ContentController(ContentService contentService, ContentDtoMapper contentDtoMapper) {
    this.contentService = contentService;
    this.contentDtoMapper = contentDtoMapper;
  }

  @Operation(summary = "컨텐츠 단건 조회 [코칭&자료 모두 조회 가능]", description = "컨텐츠(코칭&자료)를 상세 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "컨텐츠 단건 조회 성공",
        content = @Content(schema = @Schema(implementation = ContentDetailResponse.class))),
    @ApiResponse(responseCode = "404", description = "해당 컨텐츠 정보를 찾을 수 없음")
  })
  @GetMapping("/{contentId}")
  public ContentDetailResponse getContentDetail(@PathVariable("contentId") Long contentId) {
    ContentDetailDto contentDetailDto = contentService.getContentDetail(contentId);
    return contentDtoMapper.toContentDetailResponse(contentDetailDto);
  }

  @Operation(summary = "컨텐츠 임시 저장", description = "컨텐츠를 임시 저장합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "컨텐츠 임시 저장 성공",
        content = @Content(schema = @Schema(implementation = ContentResponse.class)))
  })
  @PostMapping("/draft")
  public ContentResponse saveDraft(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @Valid @RequestBody ContentDraftRequest request) {

    ContentDto contentDto = contentDtoMapper.toServiceContentDtoFromDraft(request);
    ContentDto savedContentDto =
        contentService.saveDraftAndReturn(accessor.getUserId(), contentDto);

    return contentDtoMapper.toContentDraftResponse(savedContentDto);
  }

  // 서비스 상품 심사 요청
  @Operation(summary = "컨텐츠 심사 요청", description = "작성 완료한 컨텐츠에 대해 심사를 요청합니다.")
  @PostMapping("/register")
  public ContentResponse registerContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @Valid @RequestBody ContentRegisterRequest request) {
    ContentDto contentDto = contentDtoMapper.toServiceContentDtoFromRegister(request);
    ContentDto savedContentDto = contentService.registerContent(accessor.getUserId(), contentDto);
    return contentDtoMapper.toContentDraftResponse(savedContentDto);
  }

  @Operation(summary = "컨텐츠 활성화", description = "심사완료된 컨텐츠를 활성화합니다.")
  @PostMapping("/{contentId}/active")
  public ContentStatusResponse activateContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId) {
    ContentDto contentDto = contentService.activateContent(accessor.getUserId(), contentId);
    return contentDtoMapper.toContentStatusResponse(contentDto);
  }

  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "나의 판매 상품 조회 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
  })
  @Operation(summary = "나의 판매 상품 조회", description = "나의 코칭 또는 자료 상품을 조회합니다.")
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
                  description = "상품 상태 필터 (DRAFT, PENDING, ACTIVE 등)",
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"DRAFT", "PENDING", "APPROVED", "ACTIVE"}))
              @RequestParam(value = "state", required = false)
              String state,
          @Parameter(
                  description = "상품 타입 (COACHING 또는 DOCUMENT)",
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

    String successMessage = "COACHING".equals(type) ? "나의 코칭 상품 조회 성공" : "나의 자료 상품 조회 성공";
    return ResponseEntity.ok(GrobleResponse.success(response, successMessage));
  }

  // 홈화면 상품 조회
  @Operation(summary = "홈화면 상품 조회", description = "홈화면 상품을 조회합니다.")
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
              description = "상품 타입 (COACHING 또는 DOCUMENT)",
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

    String successMessage = "COACHING".equals(type) ? "홈화면 코칭 상품 조회 성공" : "홈화면 자료 상품 조회 성공";
    return ResponseEntity.ok(GrobleResponse.success(response, successMessage));
  }

  // 홈화면 상품 조회 [자료]

  // 상품 수정
  // 상품 삭제
  // 상품 검색
  // 상품 필터링
  // 상품 정렬
  // 상품 상세 조회
  // 상품 리뷰 조회
  // 상품 리뷰 작성
  // 상품 리뷰 수정
  // 상품 리뷰 삭제
}
