package liaison.groble.api.server.content;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.request.examine.ContentExamineRequest;
import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.swagger.ContentDetail;
import liaison.groble.api.model.content.response.swagger.ContentExamine;
import liaison.groble.api.model.content.response.swagger.HomeContents;
import liaison.groble.api.server.content.mapper.ContentDtoMapper;
import liaison.groble.application.content.ContentService;
import liaison.groble.application.content.dto.ContentCardDto;
import liaison.groble.application.content.dto.ContentDetailDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "콘텐츠 관련 API", description = "콘텐츠 상세 조회, 홈화면 콘텐츠 조회")
public class ContentController {

  private final ContentService contentService;
  private final ContentDtoMapper contentDtoMapper;

  public ContentController(ContentService contentService, ContentDtoMapper contentDtoMapper) {
    this.contentService = contentService;
    this.contentDtoMapper = contentDtoMapper;
  }

  // 콘텐츠 상세 조회
  @ContentDetail
  @GetMapping("/content/{contentId}")
  public ResponseEntity<GrobleResponse<ContentDetailResponse>> getContentDetail(
      @PathVariable("contentId") Long contentId) {
    ContentDetailDto contentDetailDto = contentService.getContentDetail(contentId);
    ContentDetailResponse response = contentDtoMapper.toContentDetailResponse(contentDetailDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "콘텐츠 상세 조회 성공"));
  }

  // 홈화면 콘텐츠 조회
  @HomeContents
  @GetMapping("/home/contents")
  public ResponseEntity<GrobleResponse<List<ContentPreviewCardResponse>>> getHomeContents(
      @Parameter(
              description = "콘텐츠 타입 (COACHING 또는 DOCUMENT)",
              required = true,
              schema =
                  @Schema(
                      implementation = String.class,
                      allowableValues = {"COACHING", "DOCUMENT"}))
          @RequestParam(value = "type")
          String type) {

    // 서비스에서 콘텐츠 목록 조회 (List 형태)
    List<ContentCardDto> contentCardDtos = contentService.getHomeContentsList(type);

    // DTO 변환
    List<ContentPreviewCardResponse> responseItems =
        contentCardDtos.stream().map(contentDtoMapper::toContentPreviewCardFromCardDto).toList();

    String successMessage = "COACHING".equals(type) ? "홈화면 코칭 콘텐츠 조회 성공" : "홈화면 자료 콘텐츠 조회 성공";
    return ResponseEntity.ok(GrobleResponse.success(responseItems, successMessage));
  }

  // 콘텐츠 심사 [반려]
  @Deprecated
  @ContentExamine
  @PostMapping("/{contentId}/examine")
  public ResponseEntity<GrobleResponse<Void>> examineContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @RequestBody ContentExamineRequest examineRequest) {
    final String APPROVE = "APPROVE";
    final String REJECT = "REJECT";

    String action = examineRequest.getAction();

    if (APPROVE.equals(action)) {
      contentService.approveContent(accessor.getUserId(), contentId);
      return ResponseEntity.ok(GrobleResponse.success(null, "콘텐츠 심사 승인 성공"));
    } else if (REJECT.equals(action)) {
      // 반려 사유가 있다면 함께 전달
      String rejectReason = examineRequest.getRejectReason();
      contentService.rejectContent(accessor.getUserId(), contentId, rejectReason);
      return ResponseEntity.ok(GrobleResponse.success(null, "콘텐츠 심사 반려 성공"));
    } else {
      throw new IllegalArgumentException("지원하지 않는 심사 액션입니다: " + action);
    }
  }
}
