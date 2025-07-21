package liaison.groble.application.content.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.dto.ContentDetailDTO;
import liaison.groble.application.content.dto.ContentOptionDTO;
import liaison.groble.application.content.dto.DynamicContentDTO;
import liaison.groble.application.content.dto.review.ContentDetailReviewDTO;
import liaison.groble.application.content.dto.review.ContentReviewDTO;
import liaison.groble.application.content.dto.review.ReviewReplyDTO;
import liaison.groble.application.content.exception.InActiveContentException;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.exception.ForbiddenException;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.dto.FlatContentReviewReplyDTO;
import liaison.groble.domain.content.dto.FlatDynamicContentDTO;
import liaison.groble.domain.content.entity.Category;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.entity.DocumentOption;
import liaison.groble.domain.content.enums.AdminContentCheckingStatus;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.content.repository.CategoryRepository;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.file.entity.FileInfo;
import liaison.groble.domain.file.repository.FileRepository;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.vo.UserProfile;
import liaison.groble.external.discord.dto.ContentRegisterCreateReportDTO;
import liaison.groble.external.discord.service.content.DiscordContentRegisterReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {
  private final UserReader userReader;
  private final ContentRepository contentRepository;
  private final ContentCustomRepository contentCustomRepository;
  private final CategoryRepository categoryRepository;
  private final ContentReader contentReader;
  private final ContentReviewReader contentReviewReader;
  private final DiscordContentRegisterReportService discordContentRegisterReportService;
  private final FileRepository fileRepository;

  @Transactional(readOnly = true)
  public ContentReviewDTO getContentReviews(Long contentId, String sort) {
    List<FlatContentReviewReplyDTO> flatList =
        contentReviewReader.findReviewsWithRepliesByContentId(contentId);

    Map<Long, List<FlatContentReviewReplyDTO>> groupedByReview =
        flatList.stream().collect(Collectors.groupingBy(FlatContentReviewReplyDTO::getReviewId));

    List<ContentDetailReviewDTO> reviews =
        groupedByReview.entrySet().stream()
            .map(
                entry -> {
                  List<FlatContentReviewReplyDTO> reviewGroup = entry.getValue();
                  FlatContentReviewReplyDTO firstRow = reviewGroup.get(0);

                  List<ReviewReplyDTO> replies =
                      reviewGroup.stream()
                          .filter(row -> row.getReplyId() != null)
                          .map(
                              row ->
                                  ReviewReplyDTO.builder()
                                      .replyId(row.getReplyId())
                                      .createdAt(row.getReplyCreatedAt())
                                      .replierNickname(row.getReplierNickname())
                                      .replyContent(row.getReplyContent())
                                      .build())
                          .collect(Collectors.toList());

                  return ContentDetailReviewDTO.builder()
                      .reviewId(firstRow.getReviewId())
                      .createdAt(firstRow.getReviewCreatedAt())
                      .reviewerProfileImageUrl(firstRow.getReviewerProfileImageUrl())
                      .reviewerNickname(firstRow.getReviewerNickname())
                      .reviewContent(firstRow.getReviewContent())
                      .selectedOptionName(firstRow.getSelectedOptionName())
                      .rating(firstRow.getRating())
                      .reviewReplies(replies)
                      .build();
                })
            .sorted(getComparator(sort))
            .collect(Collectors.toList());

    BigDecimal averageRating =
        reviews.isEmpty()
            ? BigDecimal.ZERO
            : reviews.stream()
                .map(ContentDetailReviewDTO::getRating)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);

    return ContentReviewDTO.builder()
        .averageRating(averageRating)
        .totalReviewCount((long) reviews.size())
        .reviews(reviews)
        .build();
  }

  private Comparator<ContentDetailReviewDTO> getComparator(String sort) {
    switch (sort.toUpperCase()) {
      case "RATING_HIGH":
        return Comparator.comparing(ContentDetailReviewDTO::getRating).reversed();
      case "RATING_LOW":
        return Comparator.comparing(ContentDetailReviewDTO::getRating);
      case "LATEST":
      default:
        return Comparator.comparing(ContentDetailReviewDTO::getCreatedAt).reversed();
    }
  }

  @Transactional
  public ContentDTO draftContent(Long userId, ContentDTO contentDTO) {
    // 1. 사용자 조회
    User user = userReader.getUserById(userId);

    // 2. 저장할 Content 준비
    Content content;

    // 2.1 기존 Content 업데이트 또는 새 Content 생성
    if (contentDTO.getContentId() != null) {
      // 기존 Content 업데이트
      content = findAndValidateUserContent(userId, contentDTO.getContentId());
      updateContentFromDTO(content, contentDTO);

      // 기존 옵션 제거
      if (content.getOptions() != null) {
        content.getOptions().clear();
      }
    } else {
      // 새 Content 생성
      content = new Content(user);
      updateContentFromDTO(content, contentDTO);
    }

    // 3. 옵션 추가
    if (contentDTO.getOptions() != null && !contentDTO.getOptions().isEmpty()) {
      addOptionsToContent(content, contentDTO);
    }

    // 4. 저장 및 변환
    return saveAndConvertToDTO(content);
  }

  @Transactional
  public ContentDTO registerContent(Long userId, ContentDTO contentDTO) {
    validateContentForSubmission(contentDTO);

    // 2. 사용자 조회
    User user = userReader.getUserById(userId);

    // 3. Content 준비 (기존 업데이트 또는 새로 생성)
    Content content;
    if (contentDTO.getContentId() != null) {
      // 1) 기존 콘텐츠 로드 (영속 상태 보장)
      content = findAndValidateUserContent(userId, contentDTO.getContentId());

      // 2) 옵션 컬렉션을 처음부터 로딩
      //    (lazy 로딩일 경우, 강제로 컬렉션을 초기화해서
      //     이미 DB에 남아 있는 실제 엔티티만 제거되도록 함)
      content.getOptions().size();
      // → 이 시점에 Hibernate가 DB에 남아 있는 옵션 리스트를 가져옵니다.

      // 3) 컬렉션에서 실제로 제거할 대상만 남겨두고 지울 수 있도록
      //    (예: 무조건 다 지우려면 clear 그대로 사용해도 되지만,
      //     clear 직전에 fetch를 했으니 DB에 없는 id로 삭제쿼리가 나가지 않음)
      content.getOptions().clear();
    } else {
      content = new Content(user);
    }

    // 4. 카테고리 조회 및 설정 (심사 요청 시 필수)
    Category category = findCategoryByCode(contentDTO.getCategoryId());

    // 5. Content 필드 업데이트
    updateContentFromDTO(content, contentDTO);
    content.setCategory(category); // 카테고리 설정
    content.setStatus(ContentStatus.ACTIVE); // 심사중으로 설정

    // 4) 새 옵션 추가
    if (contentDTO.getOptions() != null && !contentDTO.getOptions().isEmpty()) {
      addOptionsToContent(content, contentDTO);
    }

    // 7. 저장 및 변환
    final LocalDateTime nowInSeoul = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

    final ContentRegisterCreateReportDTO contentRegisterCreateReportDTO =
        ContentRegisterCreateReportDTO.builder()
            .nickname(content.getUser().getNickname())
            .contentId(content.getId())
            .contentTitle(content.getTitle())
            .contentType(content.getContentType().name())
            .createdAt(nowInSeoul)
            .build();

    discordContentRegisterReportService.sendCreateContentRegisterReport(
        contentRegisterCreateReportDTO);

    return saveAndConvertToDTO(content);
  }

  @Transactional
  public ContentDetailDTO getContentDetailForUser(Long userId, Long contentId) {
    log.info("로그인 사용자 콘텐츠 조회: userId={}, contentId={}", userId, contentId);

    // 1. 사용자 및 콘텐츠 조회
    Content content = contentReader.getContentById(contentId);

    // 2. 콘텐츠 소유권 확인
    boolean isOwner = content.getUser().getId().equals(userId);

    if (isOwner) {
      // 내 콘텐츠인 경우: 모든 상태 조회 가능, 조회수 증가 안함
      log.info("내 콘텐츠 조회: contentId={}, status={}", contentId, content.getStatus());
    } else {
      // 다른 사용자의 콘텐츠인 경우: ACTIVE 상태만 조회 가능
      if (!ContentStatus.ACTIVE.equals(content.getStatus())) {
        log.warn(
            "비활성 콘텐츠 접근 시도: userId={}, contentId={}, status={}",
            userId,
            contentId,
            content.getStatus());
        throw new InActiveContentException("현재 판매 중이지 않은 콘텐츠입니다.");
      }

      // 조회수 증가 (다른 사용자의 콘텐츠 조회 시에만)
      content.incrementViewCount();
      contentRepository.save(content);

      log.info("다른 사용자 콘텐츠 조회: contentId={}, newViewCount={}", contentId, content.getViewCount());
    }

    // 3. getPublicContentDetail과 동일한 방식으로 DTO 변환
    // 콘텐츠 이미지 URL 목록 (현재는 썸네일만 있음)
    List<String> contentImageUrls = new ArrayList<>();
    if (content.getThumbnailUrl() != null) {
      contentImageUrls.add(content.getThumbnailUrl());
    }

    // 옵션 목록 변환 - ContentOptionDTO 사용
    List<ContentOptionDTO> optionDTOs =
        content.getOptions().stream()
            .map(
                option -> {
                  ContentOptionDTO.ContentOptionDTOBuilder builder =
                      ContentOptionDTO.builder()
                          .contentOptionId(option.getId())
                          .name(option.getName())
                          .description(option.getDescription())
                          .price(option.getPrice());

                  // 옵션 타입별 필드 설정
                  if (option instanceof DocumentOption) {
                    DocumentOption documentOption = (DocumentOption) option;
                    builder
                        .documentFileUrl(documentOption.getDocumentFileUrl())
                        .documentLinkUrl(documentOption.getDocumentLinkUrl());
                  }

                  return builder.build();
                })
            .collect(Collectors.toList());

    // User 관련 정보 추출
    User seller = content.getUser();
    String sellerProfileImageUrl = null;
    String sellerName = null;

    if (seller != null) {
      UserProfile userProfile = seller.getUserProfile();
      if (userProfile != null) {
        sellerProfileImageUrl = userProfile.getProfileImageUrl();
        sellerName = userProfile.getNickname();
      }
    }

    return ContentDetailDTO.builder()
        .contentId(content.getId())
        .status(safeEnumName(content.getStatus()))
        .thumbnailUrl(content.getThumbnailUrl())
        .contentType(safeEnumName(content.getContentType()))
        .categoryId(content.getCategory() != null ? content.getCategory().getCode() : null)
        .title(content.getTitle())
        .sellerProfileImageUrl(sellerProfileImageUrl)
        .sellerName(sellerName)
        .lowestPrice(content.getLowestPrice())
        .options(optionDTOs)
        .contentIntroduction(content.getContentIntroduction())
        .serviceTarget(content.getServiceTarget())
        .serviceProcess(content.getServiceProcess())
        .makerIntro(content.getMakerIntro())
        .build();
  }

  /**
   * 콘텐츠 상세 정보를 조회합니다.
   *
   * @param contentId 상품 ID
   * @return 상품 상세 정보
   */
  @Transactional
  public ContentDetailDTO getPublicContentDetail(Long contentId) {
    Content content = contentReader.getContentById(contentId);

    // ACTIVE 상태인지 확인
    if (!ContentStatus.ACTIVE.equals(content.getStatus())) {
      log.warn("비활성 콘텐츠 접근 시도 (비로그인): contentId={}, status={}", contentId, content.getStatus());
      throw new InActiveContentException("현재 판매 중이지 않은 콘텐츠입니다.");
    }

    // 조회수 증가
    content.incrementViewCount();
    contentRepository.save(content);

    log.info("비로그인 사용자 콘텐츠 조회: contentId={}, newViewCount={}", contentId, content.getViewCount());

    // 콘텐츠 이미지 URL 목록 (현재는 썸네일만 있음)
    List<String> contentImageUrls = new ArrayList<>();
    if (content.getThumbnailUrl() != null) {
      contentImageUrls.add(content.getThumbnailUrl());
    }

    // 옵션 목록 변환 - ContentOptionDTO 사용
    List<ContentOptionDTO> optionDTOs =
        content.getOptions().stream()
            .map(
                option -> {
                  ContentOptionDTO.ContentOptionDTOBuilder builder =
                      ContentOptionDTO.builder()
                          .contentOptionId(option.getId())
                          .name(option.getName())
                          .description(option.getDescription())
                          .price(option.getPrice());

                  // 옵션 타입별 필드 설정
                  if (option instanceof DocumentOption) {
                    DocumentOption documentOption = (DocumentOption) option;
                    builder
                        .documentFileUrl(documentOption.getDocumentFileUrl())
                        .documentLinkUrl(documentOption.getDocumentLinkUrl());
                  }

                  return builder.build();
                })
            .collect(Collectors.toList());

    // User 관련 정보 추출
    User seller = content.getUser();
    String sellerProfileImageUrl = null;
    String sellerName = null;

    if (seller != null) {
      UserProfile userProfile = seller.getUserProfile();
      if (userProfile != null) {
        sellerProfileImageUrl = userProfile.getProfileImageUrl();
        sellerName = userProfile.getNickname();
      }
    }
    return ContentDetailDTO.builder()
        .contentId(content.getId())
        .status(safeEnumName(content.getStatus()))
        .thumbnailUrl(content.getThumbnailUrl())
        .contentType(safeEnumName(content.getContentType()))
        .categoryId(content.getCategory() != null ? content.getCategory().getCode() : null)
        .title(content.getTitle())
        .sellerProfileImageUrl(sellerProfileImageUrl)
        .sellerName(sellerName)
        .lowestPrice(content.getLowestPrice())
        .options(optionDTOs)
        .contentIntroduction(content.getContentIntroduction())
        .serviceTarget(content.getServiceTarget())
        .serviceProcess(content.getServiceProcess())
        .makerIntro(content.getMakerIntro())
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentCardDTO> getMySellingContents(
      Long userId, Pageable pageable, String state) {
    List<ContentStatus> contentStatuses = parseContentStatuses(state);
    Page<FlatContentPreviewDTO> page =
        contentReader.findMyContentsWithStatus(pageable, userId, contentStatuses);
    List<ContentCardDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToCardDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  /**
   * 홈화면 콘텐츠 목록을 List 형태로 조회합니다.
   *
   * @param type 콘텐츠 타입 (COACHING 또는 DOCUMENT)
   * @return 콘텐츠 카드 DTO 목록
   */
  @Transactional(readOnly = true)
  public List<ContentCardDTO> getHomeContentsList(String type) {
    // 콘텐츠 타입 파싱
    ContentType contentType = parseContentType(type);

    // contentCustomRepository를 통해 데이터 조회 (커서 없이)
    List<FlatContentPreviewDTO> flatDTOs = contentCustomRepository.findHomeContents(contentType);

    // DTO 변환
    return flatDTOs.stream().map(this::convertFlatDTOToCardDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public String getExamineRejectReason(Long userId, Long contentId) {
    Content content = contentReader.getContentById(contentId);
    return content.getRejectReason();
  }

  // --- 유틸리티 메서드 ---

  /** Content를 저장하고 DTO로 변환합니다. */
  private ContentDTO saveAndConvertToDTO(Content content) {
    content = contentRepository.save(content);
    log.info("콘텐츠 저장 완료. ID: {}, 유저 ID: {}", content.getId(), content.getUser().getId());
    return convertToDTO(content);
  }

  /** 사용자의 Content를 찾고 접근 권한을 검증합니다. */
  private Content findAndValidateUserContent(Long userId, Long contentId) {
    Content content = contentReader.getContentById(contentId);

    if (!content.getUser().getId().equals(userId)) {
      throw new ForbiddenException("해당 콘텐츠를 수정할 권한이 없습니다.");
    }

    return content;
  }

  /** 사용자의 심사 완료된 Content를 찾고 접근 권한을 검증합니다. */
  private Content findAndValidateUserActiveContent(Long userId, Long contentId) {
    Content content = contentReader.getContentByStatusAndId(contentId, ContentStatus.ACTIVE);

    if (!content.getUser().getId().equals(userId)) {
      throw new ForbiddenException("해당 콘텐츠를 수정할 권한이 없습니다.");
    }

    return content;
  }

  /** 카테고리 ID로 카테고리를 조회합니다. */
  private Category findCategoryByCode(String categoryId) {
    if (categoryId == null) {
      throw new IllegalArgumentException("카테고리 ID는 필수입니다.");
    }

    return categoryRepository
        .findByCode(categoryId)
        .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. ID: " + categoryId));
  }

  /** DTO에서 Content 엔티티로 데이터를 업데이트합니다. */
  private void updateContentFromDTO(Content content, ContentDTO dto) {
    // 타이틀 업데이트
    if (dto.getTitle() != null) {
      content.setTitle(dto.getTitle());
    }

    // ContentType 업데이트
    if (dto.getContentType() != null) {
      try {
        content.setContentType(ContentType.valueOf(dto.getContentType()));
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 콘텐츠 유형: {}", dto.getContentType());
      }
    }

    // 썸네일 URL 업데이트
    if (dto.getThumbnailUrl() != null) {
      content.setThumbnailUrl(dto.getThumbnailUrl());
    }

    // 카테고리 업데이트 (있는 경우에만)
    if (dto.getCategoryId() != null) {
      try {
        Category category = findCategoryByCode(dto.getCategoryId());
        content.setCategory(category);
      } catch (EntityNotFoundException e) {
        log.warn("카테고리를 찾을 수 없습니다: {}", dto.getCategoryId());
      }
    }

    // 콘텐츠 소개
    if (dto.getContentIntroduction() != null) {
      content.setContentIntroduction(dto.getContentIntroduction());
    }
    // 서비스 타겟, 프로세스, 제작자 소개 업데이트
    if (dto.getServiceTarget() != null) {
      content.setServiceTarget(dto.getServiceTarget());
    }
    if (dto.getServiceProcess() != null) {
      content.setServiceProcess(dto.getServiceProcess());
    }
    if (dto.getMakerIntro() != null) {
      content.setMakerIntro(dto.getMakerIntro());
    }

    content.setStatus(ContentStatus.DRAFT);
  }

  /** 옵션을 Content에 추가하고 최저가를 설정합니다. */
  private void addOptionsToContent(Content content, ContentDTO dto) {
    // dto.getOptions()가 null인 경우 빈 리스트 사용 (NPE 방지)
    List<ContentOptionDTO> options =
        dto.getOptions() != null ? dto.getOptions() : Collections.emptyList();

    // 옵션 추가 전에 최저가 계산을 위한 리스트 생성
    List<BigDecimal> validPrices = new ArrayList<>();

    for (ContentOptionDTO optionDTO : options) {
      // null 옵션 건너뛰기
      if (optionDTO == null) continue;

      // contentType이 null인 경우 기본값 설정
      if (content.getContentType() == null) {
        log.warn("콘텐츠 유형이 지정되지 않았습니다. 기본값으로 DOCUMENT 설정");
        content.setContentType(ContentType.DOCUMENT);
      }

      // 옵션 생성 및 추가
      ContentOption option = createOptionByContentType(content.getContentType(), optionDTO);
      if (option != null) {
        content.addOption(option);

        // 유효한 가격인 경우 최저가 계산용 리스트에 추가
        if (option.getPrice() != null) {
          validPrices.add(option.getPrice());
        }
      }
    }

    // 최저가 계산 및 설정
    if (!validPrices.isEmpty()) {
      BigDecimal lowestPrice = Collections.min(validPrices);
      content.setLowestPrice(lowestPrice);
      log.debug("콘텐츠 최저가 설정: {}", lowestPrice);
    } else {
      // 유효한 가격이 없는 경우 최저가를 null로 설정
      content.setLowestPrice(null);
      log.debug("유효한 가격이 없어 최저가를 null로 설정");
    }
  }

  /** Content 유형에 맞는 옵션을 생성합니다. */
  private ContentOption createOptionByContentType(
      ContentType contentType, ContentOptionDTO optionDTO) {
    ContentOption option;

    if (contentType == ContentType.DOCUMENT) {
      option = createDocumentOption(optionDTO);
    } else {
      log.warn("지원하지 않는 콘텐츠 유형입니다: {}", contentType);
      return null;
    }

    // 공통 필드 설정
    option.setName(optionDTO.getName());
    option.setDescription(optionDTO.getDescription());
    option.setPrice(optionDTO.getPrice());

    return option;
  }

  /** 문서 옵션을 생성합니다. */
  private DocumentOption createDocumentOption(ContentOptionDTO optionDTO) {
    DocumentOption option = new DocumentOption();

    // 문서 파일 URL이 null이 아닌지 확인
    String fileUrl = optionDTO.getDocumentFileUrl();
    String documentOriginalFileName = null;
    if (fileUrl != null) {
      // 3) 실제로 FileInfo가 있는지 조회
      FileInfo oldDocumentInfo = fileRepository.findByFileUrl(fileUrl);
      if (oldDocumentInfo != null) {
        documentOriginalFileName = oldDocumentInfo.getOriginalFilename();
      }
    }

    return option;
  }

  private ContentCardDTO convertFlatDTOToCardDTO(FlatContentPreviewDTO flat) {
    return ContentCardDTO.builder()
        .contentId(flat.getContentId())
        .createdAt(flat.getCreatedAt())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .lowestPrice(flat.getLowestPrice())
        .priceOptionLength(flat.getPriceOptionLength())
        .isAvailableForSale(flat.getIsAvailableForSale())
        .status(flat.getStatus())
        .build();
  }

  /** 커서에서 Content ID를 파싱합니다. */
  private Long parseContentIdFromCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    try {
      return Long.parseLong(cursor);
    } catch (NumberFormatException e) {
      log.warn("유효하지 않은 커서 형식: {}", cursor);
      return null;
    }
  }

  private ContentType parseContentType(String type) {
    if (type == null || type.isBlank()) {
      return null;
    }

    try {
      return ContentType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않은 콘텐츠 유형: {}", type);
      return null;
    }
  }

  /** Content를 DTO로 변환합니다. */
  private ContentDTO convertToDTO(Content content) {
    // null 체크
    if (content == null) {
      return null;
    }

    // 옵션 변환
    List<ContentOptionDTO> optionDTOs = new ArrayList<>();
    if (content.getOptions() != null) {
      for (ContentOption option : content.getOptions()) {
        if (option == null) continue;

        ContentOptionDTO.ContentOptionDTOBuilder builder =
            ContentOptionDTO.builder()
                .contentOptionId(option.getId())
                .name(option.getName())
                .description(option.getDescription())
                .price(option.getPrice());

        if (option instanceof DocumentOption documentOption) {
          builder
              .documentFileUrl(
                  documentOption.getDocumentFileUrl() != null
                      ? documentOption.getDocumentFileUrl()
                      : null)
              .documentLinkUrl(
                  documentOption.getDocumentLinkUrl() != null
                      ? documentOption.getDocumentLinkUrl()
                      : null);
        }

        optionDTOs.add(builder.build());
      }
    }

    // Content DTO 구성
    ContentDTO.ContentDTOBuilder dtoBuilder =
        ContentDTO.builder()
            .contentId(content.getId())
            .title(content.getTitle())
            .thumbnailUrl(content.getThumbnailUrl())
            .options(optionDTOs.isEmpty() ? null : optionDTOs);

    // Enum을 안전하게 문자열로 변환
    if (content.getContentType() != null) {
      dtoBuilder.contentType(content.getContentType().name());
    }

    if (content.getStatus() != null) {
      dtoBuilder.status(content.getStatus().name());
    } else {
      dtoBuilder.status(ContentStatus.DRAFT.name()); // 기본값
    }

    // 카테고리가 null이 아닌 경우에만 ID 설정
    if (content.getCategory() != null) {
      dtoBuilder.categoryId(content.getCategory().getCode());
    }

    // 콘텐츠 소개와 상세 이미지 URL 추가
    if (content.getContentIntroduction() != null) {
      dtoBuilder.contentIntroduction(content.getContentIntroduction());
    }

    // 상세 이미지 URL 목록이 비어있지 않은 경우에만 추가
    //    if (content.getContentDetailImageUrls() != null
    //        && !content.getContentDetailImageUrls().isEmpty()) {
    //      dtoBuilder.contentDetailImageUrls(new ArrayList<>(content.getContentDetailImageUrls()));
    //    }

    if (content.getServiceTarget() != null) {
      dtoBuilder.serviceTarget(content.getServiceTarget());
    }

    if (content.getServiceProcess() != null) {
      dtoBuilder.serviceProcess(content.getServiceProcess());
    }

    if (content.getMakerIntro() != null) {
      dtoBuilder.makerIntro(content.getMakerIntro());
    }

    return dtoBuilder.build();
  }

  /** 심사 요청 시 필수 항목을 검증합니다. */
  private void validateContentForSubmission(ContentDTO contentDTO) {
    List<String> missingFields = new ArrayList<>();

    // 필수 필드 검증
    if (contentDTO.getTitle() == null || contentDTO.getTitle().trim().isEmpty()) {
      missingFields.add("제목");
    }

    if (contentDTO.getContentType() == null) {
      missingFields.add("콘텐츠 유형");
    }

    if (contentDTO.getCategoryId() == null) {
      missingFields.add("카테고리");
    }

    if (contentDTO.getThumbnailUrl() == null || contentDTO.getThumbnailUrl().trim().isEmpty()) {
      missingFields.add("썸네일 이미지");
    }

    //    if (contentDTO.getContentDetailImageUrls() == null
    //        || contentDTO.getContentDetailImageUrls().isEmpty()) {
    //      missingFields.add("콘텐츠 상세 이미지");
    //    }

    if (contentDTO.getContentIntroduction() == null
        || contentDTO.getContentIntroduction().trim().isEmpty()) {
      missingFields.add("콘텐츠 소개");
    }

    // 옵션 검증
    if (contentDTO.getOptions() == null || contentDTO.getOptions().isEmpty()) {
      missingFields.add("콘텐츠 옵션");
    } else {
      // 각 옵션별 필수 필드 검증
      for (int i = 0; i < contentDTO.getOptions().size(); i++) {
        ContentOptionDTO option = contentDTO.getOptions().get(i);

        if (option.getName() == null || option.getName().trim().isEmpty()) {
          missingFields.add("옵션" + (i + 1) + " 이름");
        }

        if (option.getPrice() == null) {
          missingFields.add("옵션" + (i + 1) + " 가격");
        }
      }
    }

    // 필수 필드가 누락된 경우 예외 발생
    if (!missingFields.isEmpty()) {
      throw new IllegalArgumentException(
          "심사 요청을 위해 다음 필드를 입력해주세요: " + String.join(", ", missingFields));
    }
  }

  @Transactional
  public ContentDTO stopContent(Long userId, Long contentId) {
    // 1. Content 조회 및 권한 검증
    Content content = findAndValidateUserActiveContent(userId, contentId);

    // 2. 상태 업데이트
    content.setStatus(ContentStatus.DRAFT);
    content.setAdminContentCheckingStatus(AdminContentCheckingStatus.PENDING);

    // 3. 저장 및 변환
    return saveAndConvertToDTO(content);
  }

  @Transactional
  public void deleteContent(Long userId, Long contentId) {
    // 1. Content 조회 및 권한 검증
    Content content = findAndValidateUserContent(userId, contentId);

    // 2. 삭제
    content.setStatus(ContentStatus.DELETED);
    contentRepository.save(content);
    log.info("콘텐츠 삭제 완료. 유저 ID: {}, 콘텐츠 ID: {}", userId, contentId);
  }

  /** 카테고리 ID가 null 이면 타입만, 아니면 카테고리＋타입으로 조회 */
  @Transactional(readOnly = true)
  public PageResponse<ContentCardDTO> getCoachingContentsByCategory(
      List<String> categoryIds, Pageable pageable) {

    if (categoryIds == null || categoryIds.isEmpty()) {
      // no filter: just by type
      return getContentsByType(ContentType.COACHING, pageable);
    } else {
      // filter by any of the passed categories
      return getContentsByCategoriesAndType(categoryIds, ContentType.COACHING, pageable);
    }
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentCardDTO> getDocumentContentsByCategory(
      List<String> categoryIds, Pageable pageable) {
    if (categoryIds == null || categoryIds.isEmpty()) {
      return getContentsByType(ContentType.DOCUMENT, pageable);
    } else {
      return getContentsByCategoriesAndType(categoryIds, ContentType.DOCUMENT, pageable);
    }
  }

  @Transactional
  public void convertToSale(Long userId, Long contentId) {
    // 1. Content 조회 및 권한 검증
    Content content = findAndValidateUserContent(userId, contentId);
    if (contentReader.isAvailableForSale(contentId)) {
      // 2. 상태 업데이트
      if (content.getStatus() != ContentStatus.DRAFT) {
        throw new IllegalArgumentException("콘텐츠는 DRAFT 상태여야 판매 가능 상태로 전환할 수 있습니다.");
      }

      content.setStatus(ContentStatus.ACTIVE);
    } else {
      throw new IllegalArgumentException("콘텐츠는 판매 가능한 상태가 아닙니다.");
    }
  }

  // 타입만 조회
  private PageResponse<ContentCardDTO> getContentsByType(ContentType type, Pageable pageable) {
    Page<FlatContentPreviewDTO> page = contentCustomRepository.findContentsByType(type, pageable);
    List<ContentCardDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToCardDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  // 기존 카테고리＋타입 조회
  private PageResponse<ContentCardDTO> getContentsByCategoriesAndType(
      List<String> categoryIds, ContentType type, Pageable pageable) {
    Page<FlatContentPreviewDTO> page =
        contentCustomRepository.findContentsByCategoriesAndType(categoryIds, type, pageable);

    List<ContentCardDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToCardDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .categoryIds(categoryIds)
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  public List<DynamicContentDTO> getDynamicContents() {
    List<FlatDynamicContentDTO> flatDynamicContentDTOS =
        contentCustomRepository.findAllDynamicContents();
    return flatDynamicContentDTOS.stream().map(this::convertFlatDTOToDynamicDTO).toList();
  }

  private DynamicContentDTO convertFlatDTOToDynamicDTO(FlatDynamicContentDTO flat) {
    return DynamicContentDTO.builder()
        .contentId(flat.getContentId())
        .title(flat.getTitle())
        .contentType(flat.getContentType())
        .thumbnailUrl(flat.getThumbnailUrl())
        .updatedAt(flat.getUpdatedAt())
        .build();
  }

  private String safeEnumName(Enum<?> e) {
    return e != null ? e.name() : null;
  }

  // 콘텐츠 상태 파싱 메서드 (DRAFT, ACTIVE → ACTIVE+DISCONTINUED)
  private List<ContentStatus> parseContentStatuses(String state) {
    if (state == null || state.isBlank()) {
      return Collections.emptyList();
    }

    try {
      ContentStatus contentStatus = ContentStatus.valueOf(state.toUpperCase());
      if (contentStatus == ContentStatus.ACTIVE) {
        // ACTIVE 검색 시에는 ACTIVE + DISCONTINUED 둘 다 조회
        return List.of(ContentStatus.ACTIVE, ContentStatus.DISCONTINUED);
      }
      return List.of(contentStatus);
    } catch (IllegalArgumentException e) {
      return Collections.emptyList();
    }
  }
}
