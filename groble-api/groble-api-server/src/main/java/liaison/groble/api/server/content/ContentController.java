package liaison.groble.api.server.content;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.request.examine.ContentExamineRequest;
import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.HomeContentsResponse;
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

  // 카테고리 포함 콘텐츠 조회
  //  @GetMapping("/contents/category")
  //  public ResponseEntity<GrobleResponse<PageResponse<ContentListItem>>> getContentsByCategory(
  //          @RequestParam("categoryId") Long categoryId,
  //          @RequestParam(value = "page", defaultValue = "0") int page,
  //          @RequestParam(value = "size", defaultValue = "12") int size,
  //          @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {
  //
  //      // 정렬 정보 파싱 및 페이지 요청 객체 생성
  //      String[] sortParams = sort.split(",");
  //      Sort sortObj = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
  //      Pageable pageable = PageRequest.of(page, size, sortObj);
  //
  //      // 카테고리 정보 조회 (선택적)
  //      Category category = categoryService.findById(categoryId);
  //
  //      // 서비스 호출하여 페이지 데이터 조회
  //      Page<Content> contentPage = contentService.findByCategoryId(categoryId, pageable);
  //
  //      // DTO 변환
  //      List<ContentListItem> contentItems = contentPage.getContent().stream()
  //              .map(content -> ContentListItem.builder()
  //                      .id(content.getId())
  //                      .title(content.getTitle())
  //                      .price(content.getPrice())
  //                      .seller(content.getSeller().getName())
  //                      .rating(content.getAverageRating())
  //                      .imageUrl(content.getThumbnailUrl())
  //                      .build())
  //              .collect(Collectors.toList());
  //
  //      // 메타데이터 생성 (선택적)
  //      PageResponse.MetaData metaData = PageResponse.MetaData.builder()
  //              .categoryId(categoryId)
  //              .categoryName(category.getName())
  //              .sortBy(sortParams[0])
  //              .sortDirection(sortParams[1])
  //              .build();
  //
  //      // 응답 객체 생성
  //      PageResponse<ContentListItem> response = PageResponse.from(contentPage, contentItems,
  // metaData);
  //
  //      return ResponseEntity.ok(GrobleResponse.success(response));
  //  }

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
