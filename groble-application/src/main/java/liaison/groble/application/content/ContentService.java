package liaison.groble.application.content;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.dto.ContentCardDto;
import liaison.groble.application.content.dto.ContentDetailDto;
import liaison.groble.application.content.dto.ContentDto;
import liaison.groble.application.content.dto.ContentOptionDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.exception.ForbiddenException;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Category;
import liaison.groble.domain.content.entity.CoachingOption;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.entity.DocumentOption;
import liaison.groble.domain.content.enums.CoachingPeriod;
import liaison.groble.domain.content.enums.CoachingType;
import liaison.groble.domain.content.enums.ContentDeliveryMethod;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.content.enums.DocumentProvision;
import liaison.groble.domain.content.repository.CategoryRepository;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.user.entity.User;

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

  /**
   * 콘텐츠를 임시 저장하고 저장된 정보를 반환합니다.
   *
   * @param userId 사용자 ID
   * @param contentDto 저장할 콘텐츠 정보 (null 가능)
   * @return 저장된 콘텐츠 정보
   */
  @Transactional
  public ContentDto saveDraftAndReturn(Long userId, ContentDto contentDto) {
    // 1. 사용자 조회
    User user = userReader.getUserById(userId);

    // 2. 저장할 Content 준비
    Content content;

    // 2.1 기존 Content 업데이트 또는 새 Content 생성
    if (contentDto.getContentId() != null) {
      // 기존 Content 업데이트
      content = findAndValidateUserContent(userId, contentDto.getContentId());
      updateContentFromDto(content, contentDto);

      // 기존 옵션 제거
      if (content.getOptions() != null) {
        content.getOptions().clear();
      }
    } else {
      // 새 Content 생성
      content = new Content(user);
      updateContentFromDto(content, contentDto);
    }

    // 3. 옵션 추가
    if (contentDto.getOptions() != null && !contentDto.getOptions().isEmpty()) {
      addOptionsToContent(content, contentDto);
    }

    // 4. 저장 및 변환
    return saveAndConvertToDto(content);
  }

  /**
   * 콘텐츠를 심사 요청하고 결과를 반환합니다.
   *
   * @param userId 사용자 ID
   * @param contentDto 심사 요청할 콘텐츠 정보
   * @return 심사 요청된 콘텐츠 정보
   */
  @Transactional
  public ContentDto registerContent(Long userId, ContentDto contentDto) {
    validateContentForSubmission(contentDto);

    // 2. 사용자 조회
    User user = userReader.getUserById(userId);

    // 3. Content 준비 (기존 업데이트 또는 새로 생성)
    Content content;
    if (contentDto.getContentId() != null) {
      // 기존 Content 업데이트
      content = findAndValidateUserContent(userId, contentDto.getContentId());
    } else {
      // 새 Content 생성
      content = new Content(user);
    }

    // 4. 카테고리 조회 및 설정 (심사 요청 시 필수)
    Category category = findCategoryById(contentDto.getCategoryId());

    // 5. Content 필드 업데이트
    updateContentFromDto(content, contentDto);
    content.setCategory(category); // 카테고리 설정
    content.setStatus(ContentStatus.PENDING); // 심사중으로 설정

    // 6. 기존 옵션 제거 및 새 옵션 추가
    if (content.getOptions() != null) {
      content.getOptions().clear();
    }

    if (contentDto.getOptions() != null && !contentDto.getOptions().isEmpty()) {
      addOptionsToContent(content, contentDto);
    }

    // 7. 저장 및 변환
    log.info("콘텐츠 심사 요청 완료. 유저 ID: {}", userId);
    return saveAndConvertToDto(content);
  }

  /**
   * 콘텐츠 상세 정보를 조회합니다.
   *
   * @param contentId 상품 ID
   * @return 상품 상세 정보
   */
  @Transactional(readOnly = true)
  public ContentDetailDto getContentDetail(Long contentId) {
    Content content = contentReader.getContentById(contentId);

    // 콘텐츠 이미지 URL 목록 (현재는 썸네일만 있음)
    List<String> contentImageUrls = new ArrayList<>();
    if (content.getThumbnailUrl() != null) {
      contentImageUrls.add(content.getThumbnailUrl());
    }

    // 옵션 목록 변환 - ContentOptionDto 사용
    List<ContentOptionDto> optionDtos =
        content.getOptions().stream()
            .map(
                option -> {
                  ContentOptionDto.ContentOptionDtoBuilder builder =
                      ContentOptionDto.builder()
                          .contentOptionId(option.getId())
                          .name(option.getName())
                          .description(option.getDescription())
                          .price(option.getPrice());

                  // 옵션 타입별 필드 설정
                  if (option instanceof CoachingOption) {
                    CoachingOption coachingOption = (CoachingOption) option;
                    builder
                        .coachingPeriod(coachingOption.getCoachingPeriod().name())
                        .documentProvision(coachingOption.getDocumentProvision().name())
                        .coachingType(coachingOption.getCoachingType().name())
                        .coachingTypeDescription(coachingOption.getCoachingTypeDescription());
                  } else if (option instanceof DocumentOption) {
                    DocumentOption documentOption = (DocumentOption) option;
                    builder.contentDeliveryMethod(documentOption.getContentDeliveryMethod().name());
                  }

                  return builder.build();
                })
            .collect(Collectors.toList());

    // User 관련 정보 추출
    User seller = content.getUser();
    String sellerProfileImageUrl = (seller != null) ? seller.getProfileImageUrl() : null;
    String sellerName = (seller != null) ? seller.getNickname() : null;

    return ContentDetailDto.builder()
        .contentId(content.getId())
        .status(content.getStatus().name())
        .thumbnailUrl(content.getThumbnailUrl())
        .contentType(content.getContentType().name())
        .categoryId(content.getCategory() != null ? content.getCategory().getId() : null)
        .title(content.getTitle())
        .sellerProfileImageUrl(sellerProfileImageUrl)
        .sellerName(sellerName)
        .lowestPrice(content.getLowestPrice())
        .options(optionDtos)
        .contentIntroduction(content.getContentIntroduction())
        .serviceTarget(content.getServiceTarget())
        .serviceProcess(content.getServiceProcess())
        .makerIntro(content.getMakerIntro())
        .build();
  }

  @Transactional(readOnly = true)
  public CursorResponse<ContentCardDto> getMySellingContents(
      Long userId, String cursor, int size, String state, String type) {
    Long lastContentId = parseContentIdFromCursor(cursor);
    List<ContentStatus> contentStatusList = parseContentStatusList(state);
    ContentType contentType = parseContentType(type);

    CursorResponse<FlatContentPreviewDTO> flatDtos =
        contentCustomRepository.findMySellingContentsWithCursor(
            userId, lastContentId, size, contentStatusList, contentType);

    List<ContentCardDto> cardDtos =
        flatDtos.getItems().stream()
            .map(this::convertFlatDtoToCardDto)
            .collect(Collectors.toList());

    int totalCount =
        contentCustomRepository.countMySellingContents(userId, contentStatusList, contentType);

    // 응답 구성
    return CursorResponse.<ContentCardDto>builder()
        .items(cardDtos)
        .nextCursor(flatDtos.getNextCursor())
        .hasNext(flatDtos.isHasNext())
        .totalCount(totalCount)
        .meta(flatDtos.getMeta())
        .build();
  }

  /**
   * 홈화면 콘텐츠 목록을 List 형태로 조회합니다.
   *
   * @param type 콘텐츠 타입 (COACHING 또는 DOCUMENT)
   * @return 콘텐츠 카드 DTO 목록
   */
  @Transactional(readOnly = true)
  public List<ContentCardDto> getHomeContentsList(String type) {
    // 콘텐츠 타입 파싱
    ContentType contentType = parseContentType(type);

    // contentCustomRepository를 통해 데이터 조회 (커서 없이)
    List<FlatContentPreviewDTO> flatDtos = contentCustomRepository.findHomeContents(contentType);

    // DTO 변환
    return flatDtos.stream().map(this::convertFlatDtoToCardDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CursorResponse<ContentCardDto> getHomeContents(String cursor, int size, String type) {
    Long lastContentId = parseContentIdFromCursor(cursor);
    ContentType contentType = parseContentType(type);

    CursorResponse<FlatContentPreviewDTO> flatDtos =
        contentCustomRepository.findHomeContentsWithCursor(lastContentId, size, contentType);

    List<ContentCardDto> cardDtos =
        flatDtos.getItems().stream()
            .map(this::convertFlatDtoToCardDto)
            .collect(Collectors.toList());

    return CursorResponse.<ContentCardDto>builder()
        .items(cardDtos)
        .nextCursor(flatDtos.getNextCursor())
        .hasNext(flatDtos.isHasNext())
        .build();
  }

  @Transactional(readOnly = true)
  public String getExamineRejectReason(Long userId, Long contentId) {
    Content content = contentReader.getContentById(contentId);
    return content.getRejectReason();
  }

  @Transactional
  public void approveContent(Long userId, Long contentId) {
    Content content = contentReader.getContentById(contentId);
    content.setStatus(ContentStatus.VALIDATED);
    saveAndConvertToDto(content);
  }

  @Transactional
  public void rejectContent(Long userId, Long contentId, String rejectReason) {

    Content content = contentReader.getContentById(contentId);

    content.setStatus(ContentStatus.REJECTED);
    content.setRejectReason(rejectReason);
    log.info("콘텐츠 심사 거절 완료. 유저 ID: {}, 콘텐츠 ID: {}", userId, contentId);

    saveAndConvertToDto(content);
  }

  // --- 유틸리티 메서드 ---

  /** Content를 저장하고 DTO로 변환합니다. */
  private ContentDto saveAndConvertToDto(Content content) {
    content = contentRepository.save(content);
    log.info("콘텐츠 저장 완료. ID: {}, 유저 ID: {}", content.getId(), content.getUser().getId());
    return convertToDto(content);
  }

  /** 사용자의 Content를 찾고 접근 권한을 검증합니다. */
  private Content findAndValidateUserContent(Long userId, Long contentId) {
    Content content =
        contentRepository
            .findById(contentId)
            .orElseThrow(() -> new EntityNotFoundException("콘텐츠를 찾을 수 없습니다. ID: " + contentId));

    if (!content.getUser().getId().equals(userId)) {
      throw new ForbiddenException("해당 콘텐츠를 수정할 권한이 없습니다.");
    }

    return content;
  }

  /** 사용자의 심사 완료된 Content를 찾고 접근 권한을 검증합니다. */
  private Content findAndValidateUserValidatedContent(Long userId, Long contentId) {
    Content content = contentReader.getContentByStatusAndId(contentId, ContentStatus.VALIDATED);

    if (!content.getUser().getId().equals(userId)) {
      throw new ForbiddenException("해당 콘텐츠를 수정할 권한이 없습니다.");
    }

    return content;
  }

  /** 카테고리 ID로 카테고리를 조회합니다. */
  private Category findCategoryById(Long categoryId) {
    if (categoryId == null) {
      throw new IllegalArgumentException("카테고리 ID는 필수입니다.");
    }

    return categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. ID: " + categoryId));
  }

  /** DTO에서 Content 엔티티로 데이터를 업데이트합니다. */
  private void updateContentFromDto(Content content, ContentDto dto) {
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
        Category category = findCategoryById(dto.getCategoryId());
        content.setCategory(category);
      } catch (EntityNotFoundException e) {
        log.warn("카테고리를 찾을 수 없습니다: {}", dto.getCategoryId());
      }
    }

    // 콘텐츠 소개
    if (dto.getContentIntroduction() != null) {
      content.setContentIntroduction(dto.getContentIntroduction());
    }
    // 콘텐츠 상세 이미지 URL 목록 업데이트
    if (dto.getContentDetailImageUrls() != null) {
      content.getContentDetailImageUrls().clear();
      content.getContentDetailImageUrls().addAll(dto.getContentDetailImageUrls());
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
  }

  /** 옵션을 Content에 추가하고 최저가를 설정합니다. */
  private void addOptionsToContent(Content content, ContentDto dto) {
    // dto.getOptions()가 null인 경우 빈 리스트 사용 (NPE 방지)
    List<ContentOptionDto> options =
        dto.getOptions() != null ? dto.getOptions() : Collections.emptyList();

    // 옵션 추가 전에 최저가 계산을 위한 리스트 생성
    List<BigDecimal> validPrices = new ArrayList<>();

    for (ContentOptionDto optionDto : options) {
      // null 옵션 건너뛰기
      if (optionDto == null) continue;

      // contentType이 null인 경우 기본값 설정
      if (content.getContentType() == null) {
        log.warn("콘텐츠 유형이 지정되지 않았습니다. 기본값으로 DOCUMENT 설정");
        content.setContentType(ContentType.DOCUMENT);
      }

      // 옵션 생성 및 추가
      ContentOption option = createOptionByContentType(content.getContentType(), optionDto);
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
      ContentType contentType, ContentOptionDto optionDto) {
    ContentOption option;

    if (contentType == ContentType.COACHING) {
      option = createCoachingOption(optionDto);
    } else if (contentType == ContentType.DOCUMENT) {
      option = createDocumentOption(optionDto);
    } else {
      log.warn("지원하지 않는 콘텐츠 유형입니다: {}", contentType);
      return null;
    }

    // 공통 필드 설정
    option.setName(optionDto.getName());
    option.setDescription(optionDto.getDescription());
    option.setPrice(optionDto.getPrice());

    return option;
  }

  /** 코칭 옵션을 생성합니다. */
  private CoachingOption createCoachingOption(ContentOptionDto optionDto) {
    CoachingOption option = new CoachingOption();

    // 코칭 옵션 특화 필드 설정 - null 안전하게 처리
    if (optionDto.getCoachingPeriod() != null) {
      try {
        option.setCoachingPeriod(CoachingPeriod.valueOf(optionDto.getCoachingPeriod()));
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 코칭 기간: {}", optionDto.getCoachingPeriod());
      }
    }

    if (optionDto.getDocumentProvision() != null) {
      try {
        option.setDocumentProvision(DocumentProvision.valueOf(optionDto.getDocumentProvision()));
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 자료 제공 옵션: {}", optionDto.getDocumentProvision());
      }
    }

    if (optionDto.getCoachingType() != null) {
      try {
        option.setCoachingType(CoachingType.valueOf(optionDto.getCoachingType()));
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 코칭 방식: {}", optionDto.getCoachingType());
      }
    }

    option.setCoachingTypeDescription(optionDto.getCoachingTypeDescription());

    return option;
  }

  /** 문서 옵션을 생성합니다. */
  private DocumentOption createDocumentOption(ContentOptionDto optionDto) {
    DocumentOption option = new DocumentOption();

    // 문서 옵션 특화 필드 설정 - null 안전하게 처리
    if (optionDto.getContentDeliveryMethod() != null) {
      try {
        option.setContentDeliveryMethod(
            ContentDeliveryMethod.valueOf(optionDto.getContentDeliveryMethod()));
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 콘텐츠 제공 방식: {}", optionDto.getContentDeliveryMethod());
      }
    }

    return option;
  }

  /** FlatPreviewContentDTO를 ContentCardDto로 변환합니다. */
  private ContentCardDto convertFlatDtoToCardDto(FlatContentPreviewDTO flat) {
    return ContentCardDto.builder()
        .contentId(flat.getContentId())
        .createdAt(flat.getCreatedAt())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .lowestPrice(flat.getLowestPrice())
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

  /** 문자열에서 ContentStatus를 파싱합니다. */
  private List<ContentStatus> parseContentStatusList(String state) {
    if (state == null || state.isBlank()) {
      return null;
    }

    // "APPROVED"는 특별한 경우로, VALIDATED와 REJECTED 두 상태를 모두 포함
    if ("APPROVED".equalsIgnoreCase(state)) {
      return List.of(ContentStatus.VALIDATED, ContentStatus.REJECTED);
    }

    try {
      return List.of(ContentStatus.valueOf(state.toUpperCase()));
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않은 콘텐츠 상태: {}", state);
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
  private ContentDto convertToDto(Content content) {
    // null 체크
    if (content == null) {
      return null;
    }

    // 옵션 변환
    List<ContentOptionDto> optionDtos = new ArrayList<>();
    if (content.getOptions() != null) {
      for (ContentOption option : content.getOptions()) {
        if (option == null) continue;

        ContentOptionDto.ContentOptionDtoBuilder builder =
            ContentOptionDto.builder()
                .contentOptionId(option.getId())
                .name(option.getName())
                .description(option.getDescription())
                .price(option.getPrice());

        if (option instanceof CoachingOption coachingOption) {
          builder
              .coachingPeriod(
                  coachingOption.getCoachingPeriod() != null
                      ? coachingOption.getCoachingPeriod().name()
                      : null)
              .documentProvision(
                  coachingOption.getDocumentProvision() != null
                      ? coachingOption.getDocumentProvision().name()
                      : null)
              .coachingType(
                  coachingOption.getCoachingType() != null
                      ? coachingOption.getCoachingType().name()
                      : null)
              .coachingTypeDescription(coachingOption.getCoachingTypeDescription());
        } else if (option instanceof DocumentOption documentOption) {
          builder.contentDeliveryMethod(
              documentOption.getContentDeliveryMethod() != null
                  ? documentOption.getContentDeliveryMethod().name()
                  : null);
        }

        optionDtos.add(builder.build());
      }
    }

    // Content DTO 구성
    ContentDto.ContentDtoBuilder dtoBuilder =
        ContentDto.builder()
            .contentId(content.getId())
            .title(content.getTitle())
            .thumbnailUrl(content.getThumbnailUrl())
            .options(optionDtos.isEmpty() ? null : optionDtos);

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
      dtoBuilder.categoryId(content.getCategory().getId());
    }

    // 콘텐츠 소개와 상세 이미지 URL 추가
    if (content.getContentIntroduction() != null) {
      dtoBuilder.contentIntroduction(content.getContentIntroduction());
    }

    // 상세 이미지 URL 목록이 비어있지 않은 경우에만 추가
    if (content.getContentDetailImageUrls() != null
        && !content.getContentDetailImageUrls().isEmpty()) {
      dtoBuilder.contentDetailImageUrls(new ArrayList<>(content.getContentDetailImageUrls()));
    }

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
  private void validateContentForSubmission(ContentDto contentDto) {
    List<String> missingFields = new ArrayList<>();

    // 필수 필드 검증
    if (contentDto.getTitle() == null || contentDto.getTitle().trim().isEmpty()) {
      missingFields.add("제목");
    }

    if (contentDto.getContentType() == null) {
      missingFields.add("콘텐츠 유형");
    }

    if (contentDto.getCategoryId() == null) {
      missingFields.add("카테고리");
    }

    if (contentDto.getThumbnailUrl() == null || contentDto.getThumbnailUrl().trim().isEmpty()) {
      missingFields.add("썸네일 이미지");
    }

    if (contentDto.getContentDetailImageUrls() == null
        || contentDto.getContentDetailImageUrls().isEmpty()) {
      missingFields.add("콘텐츠 상세 이미지");
    }

    if (contentDto.getContentIntroduction() == null
        || contentDto.getContentIntroduction().trim().isEmpty()) {
      missingFields.add("콘텐츠 소개");
    }

    // 옵션 검증
    if (contentDto.getOptions() == null || contentDto.getOptions().isEmpty()) {
      missingFields.add("콘텐츠 옵션");
    } else {
      // 각 옵션별 필수 필드 검증
      for (int i = 0; i < contentDto.getOptions().size(); i++) {
        ContentOptionDto option = contentDto.getOptions().get(i);

        if (option.getName() == null || option.getName().trim().isEmpty()) {
          missingFields.add("옵션" + (i + 1) + " 이름");
        }

        if (option.getPrice() == null) {
          missingFields.add("옵션" + (i + 1) + " 가격");
        }

        // 콘텐츠 유형별 옵션 필수 필드 검증
        if (ContentType.COACHING.name().equals(contentDto.getContentType())) {
          if (option.getCoachingPeriod() == null) {
            missingFields.add("옵션" + (i + 1) + " 코칭 기간");
          }
          if (option.getCoachingType() == null) {
            missingFields.add("옵션" + (i + 1) + " 코칭 방식");
          }
        } else if (ContentType.DOCUMENT.name().equals(contentDto.getContentType())) {
          if (option.getContentDeliveryMethod() == null) {
            missingFields.add("옵션" + (i + 1) + " 콘텐츠 제공 방식");
          }
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
  public ContentDto activateContent(Long userId, Long contentId) {
    // 1. Content 조회 및 권한 검증
    Content content = findAndValidateUserValidatedContent(userId, contentId);

    // 2. 상태 업데이트
    content.setStatus(ContentStatus.ACTIVE);

    // 3. 저장 및 변환
    return saveAndConvertToDto(content);
  }

  /** 카테고리 ID가 null 이면 타입만, 아니면 카테고리＋타입으로 조회 */
  @Transactional(readOnly = true)
  public PageResponse<ContentCardDto> getCoachingContentsByCategory(
      Long categoryId, Pageable pageable) {
    if (categoryId == null) {
      return getContentsByType(ContentType.COACHING, pageable);
    } else {
      return getContentsByCategoryAndType(categoryId, ContentType.COACHING, pageable);
    }
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentCardDto> getDocumentContentsByCategory(
      Long categoryId, Pageable pageable) {
    if (categoryId == null) {
      return getContentsByType(ContentType.DOCUMENT, pageable);
    } else {
      return getContentsByCategoryAndType(categoryId, ContentType.DOCUMENT, pageable);
    }
  }

  // 타입만 조회
  private PageResponse<ContentCardDto> getContentsByType(ContentType type, Pageable pageable) {
    Page<FlatContentPreviewDTO> page = contentCustomRepository.findContentsByType(type, pageable);
    List<ContentCardDto> items =
        page.getContent().stream().map(this::convertFlatDtoToCardDto).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  // 기존 카테고리＋타입 조회
  private PageResponse<ContentCardDto> getContentsByCategoryAndType(
      Long categoryId, ContentType type, Pageable pageable) {
    // 1) 카테고리 확인 (필터가 널이 아니므로 예외 처리)
    Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. ID: " + categoryId));

    Page<FlatContentPreviewDTO> page =
        contentCustomRepository.findContentsByCategoryAndType(categoryId, type, pageable);

    List<ContentCardDto> items =
        page.getContent().stream().map(this::convertFlatDtoToCardDto).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .categoryId(categoryId)
            .categoryName(category.getName())
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }
}
