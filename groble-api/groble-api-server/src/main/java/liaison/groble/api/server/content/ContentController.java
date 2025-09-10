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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.HomeContentsResponse;
import liaison.groble.api.model.content.response.review.ContentReviewResponse;
import liaison.groble.api.model.content.response.swagger.ContentsCoachingCategory;
import liaison.groble.api.model.content.response.swagger.ContentsDocumentCategory;
import liaison.groble.api.model.content.response.swagger.HomeContents;
import liaison.groble.api.model.content.response.swagger.UploadContentDownloadFile;
import liaison.groble.api.model.content.response.swagger.UploadContentThumbnail;
import liaison.groble.api.model.dashboard.request.referrer.ReferrerRequest;
import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.server.util.FileUtil;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDetailDTO;
import liaison.groble.application.content.dto.ContentViewCountDTO;
import liaison.groble.application.content.dto.review.ContentReviewDTO;
import liaison.groble.application.content.service.ContentService;
import liaison.groble.application.content.service.ContentViewCountService;
import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.application.dashboard.service.ReferrerService;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileDTO;
import liaison.groble.application.file.dto.FileUploadDTO;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.request.RequestUtil;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.content.ContentMapper;
import liaison.groble.mapping.content.ContentReviewMapper;
import liaison.groble.mapping.dashboard.ReferrerMapper;
import liaison.groble.mapping.market.MarketMapper;

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
  private static final String CONTENT_VIEW_PATH = "/content/view/{contentId}";
  private static final String CONTENT_REFERRER_PATH = "/content/referrer/{contentId}";

  // 응답 메시지 상수화
  private static final String CONTENT_DETAIL_SUCCESS_MESSAGE = "콘텐츠 상세 조회에 성공하였습니다.";
  private static final String HOME_CONTENTS_SUCCESS_MESSAGE = "홈화면 콘텐츠 조회에 성공하였습니다.";
  private static final String UPLOAD_CONTENT_THUMBNAIL_SUCCESS_MESSAGE =
      "콘텐츠 썸네일 이미지 업로드가 성공적으로 완료되었습니다.";
  private static final String CONTENT_REVIEWS_SUCCESS_MESSAGE = "콘텐츠 리뷰 목록 조회에 성공하였습니다.";
  private static final String CONTENT_VIEW_SUCCESS_MESSAGE = "콘텐츠 뷰어 화면을 성공적으로 조회했습니다.";
  private static final String CONTENT_REFERRAL_SUCCESS_MESSAGE = "콘텐츠 유입경로 저장에 성공하였습니다.";

  // Service
  private final ContentService contentService;
  private final FileService fileService;
  private final ContentViewCountService contentViewCountService;
  private final ReferrerService referrerService;

  // Mapper
  private final MarketMapper marketMapper;
  private final ContentMapper contentMapper;
  private final ContentReviewMapper contentReviewMapper;

  // Util
  private final RequestUtil requestUtil;
  private final FileUtil fileUtil;

  // Helper
  private final ResponseHelper responseHelper;
  private final ReferrerMapper referrerMapper;

  @Operation(summary = "[✅ 콘텐츠 리뷰 목록 조회]")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = ContentReviewResponse.class)))
  @GetMapping(CONTENT_REVIEWS_PATH)
  public ResponseEntity<GrobleResponse<ContentReviewResponse>> getContentReviews(
      @Auth(required = false) Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @Parameter(
              description = "정렬 기준",
              schema =
                  @Schema(
                      allowableValues = {"LATEST", "RATING_HIGH", "RATING_LOW"},
                      defaultValue = "LATEST"))
          @RequestParam(value = "sort", defaultValue = "LATEST")
          String sort) {

    ContentReviewDTO contentReviewDTO =
        contentService.getContentReviews(
            contentId, sort, accessor.getUserId() // null if not authenticated
            );
    ContentReviewResponse response = contentReviewMapper.toContentReviewResponse(contentReviewDTO);
    return responseHelper.success(response, CONTENT_REVIEWS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 콘텐츠 상세 조회
  @Operation(summary = "[✅ 콘텐츠 상세 정보 조회]", description = "콘텐츠 상세를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = CONTENT_DETAIL_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentDetailResponse.class)))
  @Logging(item = "Content", action = "getContentDetail", includeParam = true, includeResult = true)
  @GetMapping(CONTENT_DETAIL_PATH)
  public ResponseEntity<GrobleResponse<ContentDetailResponse>> getContentDetail(
      @Auth(required = false) Accessor accessor, @PathVariable("contentId") Long contentId) {
    ContentDetailDTO contentDetailDTO;

    // 왜 인증된 사용자와 인증되지 않은 사용자를 나눴었지?
    if (accessor.isAuthenticated()) {
      contentDetailDTO = contentService.getContentDetailForUser(accessor.getId(), contentId);
    } else {
      contentDetailDTO = contentService.getPublicContentDetail(contentId);
    }

    ContactInfoDTO contactInfoDTO = contentService.getContactInfo(contentId);
    ContactInfoResponse contactInfoResponse = marketMapper.toContactInfoResponse(contactInfoDTO);

    ContentDetailResponse response =
        contentMapper.toContentDetailResponse(contentDetailDTO, contactInfoResponse);
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
          fileUtil.toServiceFileUploadDTO(contentThumbnailImage, "contents/thumbnail");
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
          fileUtil.toServiceFileUploadDTO(contentDocumentFile, "contents/document");
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

  @Operation(
      summary = "[✅ 콘텐츠 뷰어] 콘텐츠 뷰어 화면 조회",
      description =
          "만료 시간 1시간 이내의 중복 조회를 방지하며, 콘텐츠 뷰어 화면을 조회합니다. "
              + "조회수는 1시간 동안 중복되지 않으며, 이후에는 다시 조회수가 증가합니다.")
  @Logging(item = "Content", action = "viewContent", includeParam = true, includeResult = true)
  @PostMapping(CONTENT_VIEW_PATH)
  public ResponseEntity<GrobleResponse<Void>> viewContent(
      @Auth(required = false) Accessor accessor, @PathVariable("contentId") Long contentId) {
    ContentViewCountDTO contentViewCountDTO =
        ContentViewCountDTO.builder()
            .userId(accessor.getUserId())
            .ip(requestUtil.getClientIp())
            .userAgent(requestUtil.getUserAgent())
            .referer(requestUtil.getReferer())
            .build();

    contentViewCountService.recordContentView(contentId, contentViewCountDTO);

    return responseHelper.success(null, CONTENT_VIEW_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Logging(item = "Content", action = "viewContent", includeParam = true, includeResult = true)
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
        FileUploadDTO dto = fileUtil.toServiceFileUploadDTO(file, "contents/detail");
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

  @Logging(
      item = "Content",
      action = "recordContentReferrer",
      includeParam = true,
      includeResult = true)
  @PostMapping(CONTENT_REFERRER_PATH)
  public ResponseEntity<GrobleResponse<Void>> recordContentReferrer(
      @PathVariable("contentId") Long contentId,
      @Valid @RequestBody ReferrerRequest referrerRequest) {
    ReferrerDTO referrerDTO = referrerMapper.toContentReferrerDTO(referrerRequest);
    referrerService.recordContentReferrer(contentId, referrerDTO);
    return responseHelper.success(null, CONTENT_REFERRAL_SUCCESS_MESSAGE, HttpStatus.OK);
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
