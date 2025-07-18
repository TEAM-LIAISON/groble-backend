package liaison.groble.api.server.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.HomeContentsResponse;
import liaison.groble.api.model.content.response.review.ContentReviewResponse;
import liaison.groble.api.model.content.response.swagger.ContentDetail;
import liaison.groble.api.model.content.response.swagger.ContentsCoachingCategory;
import liaison.groble.api.model.content.response.swagger.ContentsDocumentCategory;
import liaison.groble.api.model.content.response.swagger.HomeContents;
import liaison.groble.api.model.content.response.swagger.UploadContentDetailImages;
import liaison.groble.api.model.content.response.swagger.UploadContentDownloadFile;
import liaison.groble.api.model.content.response.swagger.UploadContentThumbnail;
import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.api.server.util.FileUtils;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDetailDTO;
import liaison.groble.application.content.dto.review.ContentReviewDTO;
import liaison.groble.application.content.service.ContentService;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileDTO;
import liaison.groble.application.file.dto.FileUploadDTO;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.content.ContentMapper;
import liaison.groble.mapping.content.ContentReviewMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@Tag(name = "[📝 콘텐츠] 콘텐츠 관련 API", description = "콘텐츠 상세 조회, 콘텐츠 리뷰 목록 조회, 콘텐츠 이미지 업로드 등")
public class ContentController {

  // API 경로 상수화
  private static final String CONTENT_DETAIL_PATH = "/content/{contentId}";
  private static final String HOME_CONTENTS_PATH = "/home/contents";
  private static final String UPLOAD_CONTENT_THUMBNAIL_PATH = "/content/thumbnail/image";
  private static final String UPLOAD_CONTENT_DETAIL_IMAGES_PATH = "/content/detail/images";
  private static final String CONTENT_COACHING_CATEGORY_PATH = "/contents/coaching/category";
  private static final String CONTENT_DOCUMENT_CATEGORY_PATH = "/contents/document/category";
  private static final String CONTENT_REVIEWS_PATH = "/content/{contentId}/reviews";

  // 응답 메시지 상수화
  private static final String CONTENT_DETAIL_SUCCESS_MESSAGE = "콘텐츠 상세 조회에 성공하였습니다.";
  private static final String HOME_CONTENTS_SUCCESS_MESSAGE = "홈화면 콘텐츠 조회에 성공하였습니다.";
  private static final String UPLOAD_CONTENT_THUMBNAIL_SUCCESS_MESSAGE =
      "콘텐츠 썸네일 이미지 업로드가 성공적으로 완료되었습니다.";
  private static final String CONTENT_REVIEWS_SUCCESS_MESSAGE = "콘텐츠 리뷰 목록 조회에 성공하였습니다.";

  // Service
  private final ContentService contentService;
  private final FileService fileService;

  // Mapper
  private final ContentMapper contentMapper;
  private final ContentReviewMapper contentReviewMapper;
  private final FileUtils fileUtils;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(summary = "[✅ 콘텐츠 리뷰 목록 조회]")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = ContentReviewResponse.class)))
  @GetMapping(CONTENT_REVIEWS_PATH)
  public ResponseEntity<GrobleResponse<ContentReviewResponse>> getContentReviews(
      @PathVariable("contentId") Long contentId,
      @Parameter(
              description = "정렬 기준",
              schema =
                  @Schema(
                      allowableValues = {"LATEST", "RATING_HIGH", "RATING_LOW"},
                      defaultValue = "LATEST"))
          @RequestParam(value = "sort", defaultValue = "LATEST")
          String sort) {

    ContentReviewDTO contentReviewDTO = contentService.getContentReviews(contentId, sort);

    ContentReviewResponse response = contentReviewMapper.toContentReviewResponse(contentReviewDTO);
    return responseHelper.success(response, CONTENT_REVIEWS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @ContentDetail
  @GetMapping(CONTENT_DETAIL_PATH)
  public ResponseEntity<GrobleResponse<ContentDetailResponse>> getContentDetail(
      @Auth(required = false) Accessor accessor, @PathVariable("contentId") Long contentId) {
    ContentDetailDTO contentDetailDTO;

    if (accessor.isAuthenticated()) {
      contentDetailDTO = contentService.getContentDetailForUser(accessor.getId(), contentId);
    } else {
      contentDetailDTO = contentService.getPublicContentDetail(contentId);
    }

    ContentDetailResponse response = contentMapper.toContentDetailResponse(contentDetailDTO);
    return responseHelper.success(response, CONTENT_DETAIL_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 홈화면 콘텐츠 조회
  @HomeContents
  @GetMapping(HOME_CONTENTS_PATH)
  public ResponseEntity<GrobleResponse<HomeContentsResponse>> getHomeContents() {
    List<ContentCardDTO> coachingContentCardDTOS = contentService.getHomeContentsList("COACHING");
    List<ContentPreviewCardResponse> coachingItems =
        coachingContentCardDTOS.stream().map(contentMapper::toContentPreviewCardResponse).toList();

    List<ContentCardDTO> documentContentCardDTOS = contentService.getHomeContentsList("DOCUMENT");
    List<ContentPreviewCardResponse> documentItems =
        documentContentCardDTOS.stream().map(contentMapper::toContentPreviewCardResponse).toList();

    // Wrapper DTO에 담기
    HomeContentsResponse payload = new HomeContentsResponse(coachingItems, documentItems);
    return responseHelper.success(payload, HOME_CONTENTS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @ContentsCoachingCategory
  @GetMapping(CONTENT_COACHING_CATEGORY_PATH)
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

    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<ContentCardDTO> dtoPage =
        contentService.getCoachingContentsByCategory(categoryIds, pageable);

    PageResponse<ContentPreviewCardResponse> responsePage = toPreviewResponsePage(dtoPage);
    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  @ContentsDocumentCategory
  @GetMapping(CONTENT_DOCUMENT_CATEGORY_PATH)
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

    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<ContentCardDTO> dtoPage =
        contentService.getDocumentContentsByCategory(categoryIds, pageable);

    PageResponse<ContentPreviewCardResponse> responsePage = toPreviewResponsePage(dtoPage);
    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  // ContentCardDTO → ContentPreviewCardResponse + PageResponse 재구성 헬퍼
  private PageResponse<ContentPreviewCardResponse> toPreviewResponsePage(
      PageResponse<ContentCardDTO> dtoPage) {
    List<ContentPreviewCardResponse> items =
        dtoPage.getItems().stream()
            .map(contentMapper::toContentPreviewCardResponse)
            .collect(Collectors.toList());

    return PageResponse.<ContentPreviewCardResponse>builder()
        .items(items)
        .pageInfo(dtoPage.getPageInfo())
        .meta(dtoPage.getMeta())
        .build();
  }

  @UploadContentThumbnail
  @PostMapping(UPLOAD_CONTENT_THUMBNAIL_PATH)
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
      FileUploadDTO fileUploadDTO =
          fileUtils.toServiceFileUploadDTO(contentThumbnailImage, "contents/thumbnail");
      FileDTO fileDTO = fileService.uploadFile(accessor.getUserId(), fileUploadDTO);
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDTO.getOriginalFilename(),
              fileDTO.getFileUrl(),
              fileDTO.getContentType(),
              "contents/thumbnail");
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              GrobleResponse.success(
                  response, "썸네일 이미지 업로드가 성공적으로 완료되었습니다.", HttpStatus.CREATED.value()));
    } catch (IOException ioe) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GrobleResponse.error(
                  "썸네일 저장 중 오류가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  @UploadContentDetailImages
  @PostMapping(UPLOAD_CONTENT_DETAIL_IMAGES_PATH)
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
        FileUploadDTO dto = fileUtils.toServiceFileUploadDTO(file, "contents/detail");
        FileDTO uploaded = fileService.uploadFile(accessor.getUserId(), dto);
        responses.add(
            FileUploadResponse.of(
                uploaded.getOriginalFilename(),
                uploaded.getFileUrl(),
                uploaded.getContentType(),
                "contents/detail"));
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

  // 콘텐츠의 즉시 다운로드 파일 객체 저장 요청
  @UploadContentDownloadFile
  @PostMapping("/content/document/upload/file")
  public ResponseEntity<GrobleResponse<?>> addContentDocumentFile(
      @Auth Accessor accessor,
      @RequestPart("contentDocumentFile")
          @Parameter(
              description = "콘텐츠 자료 파일",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          @Valid
          final MultipartFile contentDocumentFile) {
    if (contentDocumentFile == null || contentDocumentFile.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("콘텐츠 파일을 업로드해주세요.", HttpStatus.BAD_REQUEST.value()));
    }

    if (!isPdfAndZipFile(contentDocumentFile)) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("pdf/zip 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST.value()));
    }
    try {
      FileUploadDTO fileUploadDTO =
          fileUtils.toServiceFileUploadDTO(contentDocumentFile, "contents/document");
      FileDTO fileDTO = fileService.uploadFile(accessor.getUserId(), fileUploadDTO);
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDTO.getOriginalFilename(),
              fileDTO.getFileUrl(),
              fileDTO.getContentType(),
              "contents/document");
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              GrobleResponse.success(
                  response, "콘텐츠 자료 업로드가 성공적으로 완료되었습니다.", HttpStatus.CREATED.value()));
    } catch (IOException ioe) {
      // I/O 문제(파일 읽기 실패 등)
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GrobleResponse.error(
                  "콘텐츠 자료 저장 중 오류가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /** 이미지 파일 여부 확인 */
  private boolean isImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
  }

  private boolean isPdfAndZipFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null
        && (contentType.equals("application/pdf") || contentType.equals("application/zip"));
  }
}
