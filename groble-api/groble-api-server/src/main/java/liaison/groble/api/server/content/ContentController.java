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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
@Tag(name = "서비스 상품 API", description = "서비스 상품 조회 및 등록(임시 저장, 심사 요청) API")
public class ContentController {

  private final ContentService contentService;
  private final ContentDtoMapper contentDtoMapper;

  // 서비스 상품 단건 조회 (상세 조회)
  @Operation(summary = "서비스 상품 단건 조회", description = "서비스 상품을 상세 조회합니다.")
  @GetMapping("/{contentId}")
  public ResponseEntity<GrobleResponse<ContentDetailResponse>> getContentDetail(
      @PathVariable("contentId") Long contentId) {

    // 서비스 상품 상세 조회 로직
    ContentDetailDto contentDetailDto = contentService.getContentDetail(contentId);
    // DTO 매핑
    ContentDetailResponse response = contentDtoMapper.toContentDetailResponse(contentDetailDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "서비스 상품 상세 조회 성공"));
  }

  // 서비스 상품 임시 저장
  @Operation(summary = "서비스 상품 임시 저장", description = "서비스 상품을 임시 저장합니다.")
  @ApiResponse(description = "서비스 상품을 임시 저장합니다.")
  @PostMapping("/draft")
  public ResponseEntity<GrobleResponse<ContentResponse>> saveDraft(
      @Auth Accessor accessor, @Valid @RequestBody ContentDraftRequest request) {
    // 1. 요청 DTO를 서비스 DTO로 변환
    ContentDto contentDto = contentDtoMapper.toServiceContentDtoFromDraft(request);

    // 2. 서비스 호출 (저장 및 결과 반환 - 단일 트랜잭션)
    ContentDto savedContentDto =
        contentService.saveDraftAndReturn(accessor.getUserId(), contentDto);

    // 3. 응답 DTO로 변환
    ContentResponse response = contentDtoMapper.toContentDraftResponse(savedContentDto);

    return ResponseEntity.ok(GrobleResponse.success(response, "서비스 상품 임시 저장 성공"));
  }

  // 서비스 상품 심사 요청
  @Operation(summary = "서비스 상품 심사 요청", description = "서비스 상품을 심사 요청합니다.")
  @PostMapping("/register")
  public ResponseEntity<GrobleResponse<ContentResponse>> registerContent(
      @Auth Accessor accessor, @Valid @RequestBody ContentRegisterRequest request) {
    // 1. 요청 DTO를 서비스 DTO로 변환
    ContentDto contentDto = contentDtoMapper.toServiceContentDtoFromRegister(request);
    ContentDto savedContentDto = contentService.registerContent(accessor.getUserId(), contentDto);

    ContentResponse response = contentDtoMapper.toContentDraftResponse(savedContentDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "서비스 상품 심사 요청 성공"));
  }

  // 나의 코칭 상품 (판매자)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "나의 코칭 상품 조회 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
  })
  @Operation(summary = "나의 코칭 상품 조회", description = "나의 코칭 상품을 조회합니다.")
  @GetMapping("/my/coaching")
  public ResponseEntity<GrobleResponse<CursorResponse<ContentPreviewCardResponse>>>
      getMyCoachingContents(
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
                          allowableValues = {
                            "DRAFT",
                            "PENDING",
                            "ACTIVE",
                            "REJECTED",
                            "SUSPENDED"
                          }))
              @RequestParam(value = "state", required = false)
              String state) {
    // 서비스 호출
    CursorResponse<ContentCardDto> cardDtos =
        contentService.getMyCoachingContents(
            accessor.getUserId(), cursorRequest.getCursor(), cursorRequest.getSize(), state);

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

    return ResponseEntity.ok(GrobleResponse.success(response, "나의 코칭 상품 조회 성공"));
  }

  // 상품 등록
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
