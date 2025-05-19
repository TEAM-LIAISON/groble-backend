package liaison.groble.api.server.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.api.model.content.request.examine.ContentExamineRequest;
import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.HomeContentsResponse;
import liaison.groble.api.model.content.response.swagger.ContentDetail;
import liaison.groble.api.model.content.response.swagger.ContentExamine;
import liaison.groble.api.model.content.response.swagger.ContentsCoachingCategory;
import liaison.groble.api.model.content.response.swagger.ContentsDocumentCategory;
import liaison.groble.api.model.content.response.swagger.HomeContents;
import liaison.groble.api.model.content.response.swagger.UploadContentDetailImages;
import liaison.groble.api.model.content.response.swagger.UploadContentThumbnail;
import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.api.server.content.mapper.ContentDtoMapper;
import liaison.groble.api.server.file.mapper.FileDtoMapper;
import liaison.groble.application.content.ContentService;
import liaison.groble.application.content.dto.ContentCardDto;
import liaison.groble.application.content.dto.ContentDetailDto;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileDto;
import liaison.groble.application.file.dto.FileUploadDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "콘텐츠 관련 API", description = "콘텐츠 상세 조회, 홈화면 콘텐츠 조회")
public class ContentController {

  private final ContentService contentService;
  private final ContentDtoMapper contentDtoMapper;
  private final FileService fileService;
  private final FileDtoMapper fileDtoMapper;

  public ContentController(
      ContentService contentService,
      ContentDtoMapper contentDtoMapper,
      FileService fileService,
      FileDtoMapper fileDtoMapper) {
    this.contentService = contentService;
    this.contentDtoMapper = contentDtoMapper;
    this.fileService = fileService;
    this.fileDtoMapper = fileDtoMapper;
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
          @Parameter(
                  in = ParameterIn.QUERY,
                  name = "categoryId",
                  description = "카테고리 코드 (여러 개 전달 가능)",
                  array = @ArraySchema(schema = @Schema(type = "string")))
              @RequestParam(value = "categoryId", required = false)
              List<String> categoryIds,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "12") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt,popular") String sort) {

    Pageable pageable = createPageable(page, size, sort);
    PageResponse<ContentCardDto> dtoPage =
        contentService.getCoachingContentsByCategory(categoryIds, pageable);

    PageResponse<ContentPreviewCardResponse> responsePage = toPreviewResponsePage(dtoPage);
    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  @ContentsDocumentCategory
  @GetMapping("/contents/document/category")
  public ResponseEntity<GrobleResponse<PageResponse<ContentPreviewCardResponse>>>
      getDocumentContentsByCategory(
          @Parameter(
                  in = ParameterIn.QUERY,
                  name = "categoryId",
                  description = "카테고리 코드 (여러 개 전달 가능)",
                  array = @ArraySchema(schema = @Schema(type = "string")))
              @RequestParam(value = "categoryId", required = false)
              List<String> categoryIds,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "12") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt,popular") String sort) {

    Pageable pageable = createPageable(page, size, sort);
    PageResponse<ContentCardDto> dtoPage =
        contentService.getDocumentContentsByCategory(categoryIds, pageable);

    PageResponse<ContentPreviewCardResponse> responsePage = toPreviewResponsePage(dtoPage);
    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  // ------------------------
  // PageRequest 생성 헬퍼
  /** PageRequest 생성 헬퍼 */
  private Pageable createPageable(int page, int size, String sort) {
    String[] parts = sort.split(",");
    String key = parts[0].trim();
    Sort.Direction direction;
    if (parts.length > 1) {
      try {
        direction = Sort.Direction.fromString(parts[1].trim());
      } catch (IllegalArgumentException e) {
        direction = Sort.Direction.DESC;
      }
    } else {
      direction = Sort.Direction.DESC;
    }

    // "popular" 로 넘어오면 viewCount 컬럼 기준 정렬
    if ("popular".equalsIgnoreCase(key)) {
      return PageRequest.of(page, size, Sort.by(direction, "viewCount"));
    }

    // 그 외엔 key 그대로
    return PageRequest.of(page, size, Sort.by(direction, key));
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

  // 콘텐츠 심사
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

  // 콘텐츠의 썸네일 이미지 저장 요청
  @UploadContentThumbnail
  @PostMapping("/content/thumbnail/image")
  public ResponseEntity<GrobleResponse<?>> addContentThumbnailImage(
      @Auth final Accessor accessor,
      @RequestPart("contentThumbnailImage")
          @Parameter(
              description = "콘텐츠 썸네일 이미지 파일",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          @Valid
          final MultipartFile contentThumbnailImage) {

    if (contentThumbnailImage == null || contentThumbnailImage.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("이미지 파일을 선택해주세요.", HttpStatus.BAD_REQUEST.value()));
    }

    if (!isImageFile(contentThumbnailImage)) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("이미지 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST.value()));
    }
    try {
      FileUploadDto fileUploadDto =
          fileDtoMapper.toServiceFileUploadDto(contentThumbnailImage, "contents/thumbnail");
      FileDto fileDto = fileService.uploadFile(accessor.getUserId(), fileUploadDto);
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDto.getOriginalFilename(),
              fileDto.getFileUrl(),
              fileDto.getContentType(),
              "/contents/thumbnail");
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              GrobleResponse.success(
                  response, "썸네일 이미지 업로드가 성공적으로 완료되었습니다.", HttpStatus.CREATED.value()));
    } catch (IOException ioe) {
      // I/O 문제(파일 읽기 실패 등)
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GrobleResponse.error(
                  "썸네일 저장 중 오류가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  @UploadContentDetailImages
  @PostMapping("/content/detail/images")
  public ResponseEntity<GrobleResponse<?>> addContentDetailImages(
      @Auth Accessor accessor,
      @RequestPart("contentDetailImages")
          @Parameter(
              description = "콘텐츠 상세 이미지 파일들 (여러 개 가능)",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                      array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))))
          List<MultipartFile> contentDetailImages) {

    if (contentDetailImages == null || contentDetailImages.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("적어도 하나 이상의 이미지를 선택해주세요.", HttpStatus.BAD_REQUEST.value()));
    }

    List<FileUploadResponse> responses = new ArrayList<>();
    for (MultipartFile file : contentDetailImages) {
      if (file.isEmpty() || !isImageFile(file)) {
        return ResponseEntity.badRequest()
            .body(GrobleResponse.error("모든 파일이 유효한 이미지여야 합니다.", HttpStatus.BAD_REQUEST.value()));
      }
      try {
        FileUploadDto dto = fileDtoMapper.toServiceFileUploadDto(file, "contents/detail");
        FileDto uploaded = fileService.uploadFile(accessor.getUserId(), dto);
        responses.add(
            FileUploadResponse.of(
                uploaded.getOriginalFilename(),
                uploaded.getFileUrl(),
                uploaded.getContentType(),
                "/contents/detail"));
      } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                GrobleResponse.error(
                    "상세 이미지 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
      }
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            GrobleResponse.success(
                responses, "상세 이미지 업로드가 성공적으로 완료되었습니다.", HttpStatus.CREATED.value()));
  }

  /** 이미지 파일 여부 확인 */
  private boolean isImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
  }
}
