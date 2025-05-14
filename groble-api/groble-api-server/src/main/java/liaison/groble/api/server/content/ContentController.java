package liaison.groble.api.server.content;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import liaison.groble.api.model.content.response.HomeContentsResponse;
import liaison.groble.api.model.content.response.swagger.ContentDetail;
import liaison.groble.api.model.content.response.swagger.ContentExamine;
import liaison.groble.api.model.content.response.swagger.ContentsCoachingCategory;
import liaison.groble.api.model.content.response.swagger.ContentsDocumentCategory;
import liaison.groble.api.model.content.response.swagger.HomeContents;
import liaison.groble.api.server.content.mapper.ContentDtoMapper;
import liaison.groble.application.content.ContentService;
import liaison.groble.application.content.dto.ContentCardDto;
import liaison.groble.application.content.dto.ContentDetailDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Parameter;
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
  public ResponseEntity<GrobleResponse<HomeContentsResponse>> getHomeContents() {

    // 서비스에서 콘텐츠 목록 조회 (List 형태)
    List<ContentCardDto> coachingContentCardDtos = contentService.getHomeContentsList("COACHING");
    List<ContentPreviewCardResponse> coachingItems =
        coachingContentCardDtos.stream()
            .map(contentDtoMapper::toContentPreviewCardFromCardDto)
            .toList();

    List<ContentCardDto> documentContentCardDtos = contentService.getHomeContentsList("DOCUMENT");
    List<ContentPreviewCardResponse> documentItems =
        documentContentCardDtos.stream()
            .map(contentDtoMapper::toContentPreviewCardFromCardDto)
            .toList();

    // 래퍼 DTO에 담기
    HomeContentsResponse payload = new HomeContentsResponse(coachingItems, documentItems);

    String successMessage = "홈화면 콘텐츠 조회 성공";
    return ResponseEntity.ok(GrobleResponse.success(payload, successMessage));
  }

  @ContentsCoachingCategory
  @GetMapping("/contents/coaching/category")
  public ResponseEntity<GrobleResponse<PageResponse<ContentPreviewCardResponse>>>
      getCoachingContentsByCategory(
          @RequestParam(value = "categoryId", required = false) Long categoryId,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "12") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {

    Pageable pageable = createPageable(page, size, sort);
    PageResponse<ContentCardDto> dtoPage =
        contentService.getCoachingContentsByCategory(categoryId, pageable);

    PageResponse<ContentPreviewCardResponse> responsePage = toPreviewResponsePage(dtoPage);
    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  @ContentsDocumentCategory
  @GetMapping("/contents/document/category")
  public ResponseEntity<GrobleResponse<PageResponse<ContentPreviewCardResponse>>>
      getDocumentContentsByCategory(
          @RequestParam(value = "categoryId", required = false) Long categoryId,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "12") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {

    Pageable pageable = createPageable(page, size, sort);
    PageResponse<ContentCardDto> dtoPage =
        contentService.getDocumentContentsByCategory(categoryId, pageable);

    PageResponse<ContentPreviewCardResponse> responsePage = toPreviewResponsePage(dtoPage);
    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  // ------------------------
  // PageRequest 생성 헬퍼
  private Pageable createPageable(int page, int size, String sort) {
    String[] parts = sort.split(",");
    String property = parts[0]; // 최소한 필드명은 있다고 가정
    Sort.Direction direction;
    if (parts.length > 1) {
      try {
        direction = Sort.Direction.fromString(parts[1]);
      } catch (IllegalArgumentException e) {
        // 잘못된 방향이 넘어온 경우에도 기본값 사용
        direction = Sort.Direction.DESC;
      }
    } else {
      // 방향이 명시되지 않은 경우 기본 DESC
      direction = Sort.Direction.DESC;
    }
    return PageRequest.of(page, size, Sort.by(direction, property));
  }

  // ContentCardDto → ContentPreviewCardResponse + PageResponse 재구성 헬퍼
  private PageResponse<ContentPreviewCardResponse> toPreviewResponsePage(
      PageResponse<ContentCardDto> dtoPage) {
    List<ContentPreviewCardResponse> items =
        dtoPage.getItems().stream()
            .map(contentDtoMapper::toContentPreviewCardFromCardDto)
            .collect(Collectors.toList());

    return PageResponse.<ContentPreviewCardResponse>builder()
        .items(items)
        .pageInfo(dtoPage.getPageInfo())
        .meta(dtoPage.getMeta())
        .build();
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
