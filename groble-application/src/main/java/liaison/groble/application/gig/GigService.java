package liaison.groble.application.gig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.gig.dto.GigCardDto;
import liaison.groble.application.gig.dto.GigDetailDto;
import liaison.groble.application.gig.dto.GigDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.exception.ForbiddenException;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.gig.dto.FlatPreviewGigDTO;
import liaison.groble.domain.gig.entity.Category;
import liaison.groble.domain.gig.entity.CoachingOption;
import liaison.groble.domain.gig.entity.DocumentOption;
import liaison.groble.domain.gig.entity.Gig;
import liaison.groble.domain.gig.entity.GigOption;
import liaison.groble.domain.gig.enums.CoachingPeriod;
import liaison.groble.domain.gig.enums.CoachingType;
import liaison.groble.domain.gig.enums.ContentDeliveryMethod;
import liaison.groble.domain.gig.enums.DocumentProvision;
import liaison.groble.domain.gig.enums.GigStatus;
import liaison.groble.domain.gig.enums.GigType;
import liaison.groble.domain.gig.repository.CategoryRepository;
import liaison.groble.domain.gig.repository.GigCustomRepository;
import liaison.groble.domain.gig.repository.GigRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GigService {
  private final UserReader userReader;
  private final GigRepository gigRepository;
  private final GigCustomRepository gigCustomRepository;
  private final CategoryRepository categoryRepository;

  /**
   * 서비스 상품을 임시 저장하고 저장된 정보를 반환합니다.
   *
   * @param userId 사용자 ID
   * @param gigDto 저장할 상품 정보 (null 가능)
   * @return 저장된 상품 정보
   */
  @Transactional
  public GigDto saveDraftAndReturn(Long userId, GigDto gigDto) {
    // 1. 사용자 조회
    User user = userReader.getUserById(userId);

    // 2. 저장할 Gig 준비
    Gig gig;

    // 2.1 입력 DTO가 null인 경우 기본 Gig 생성
    if (gigDto == null) {
      log.info("임시 저장 요청에 데이터가 없습니다. 기본 상품 생성");
      gig = new Gig(user);
      gig.setTitle("새 서비스 상품");
      return saveAndConvertToDto(gig);
    }

    // 2.2 기존 Gig 업데이트 또는 새 Gig 생성
    if (gigDto.getGigId() != null) {
      // 기존 Gig 업데이트
      gig = findAndValidateUserGig(userId, gigDto.getGigId());
      updateGigFromDto(gig, gigDto);

      // 기존 옵션 제거
      if (gig.getOptions() != null) {
        gig.getOptions().clear();
      }
    } else {
      // 새 Gig 생성
      gig = new Gig(user);
      updateGigFromDto(gig, gigDto);
    }

    // 3. 옵션 추가
    if (gigDto.getOptions() != null && !gigDto.getOptions().isEmpty()) {
      addOptionsToGig(gig, gigDto);
    }

    // 4. 저장 및 변환
    return saveAndConvertToDto(gig);
  }

  /**
   * 서비스 상품을 심사 요청하고 결과를 반환합니다.
   *
   * @param userId 사용자 ID
   * @param gigDto 심사 요청할 상품 정보
   * @return 심사 요청된 상품 정보
   */
  @Transactional
  public GigDto registerGig(Long userId, GigDto gigDto) {
    // 1. 유효성 검증
    if (gigDto == null) {
      throw new IllegalArgumentException("심사 요청할 상품 정보가 필요합니다.");
    }

    validateGigForSubmission(gigDto);

    // 2. 사용자 조회
    User user = userReader.getUserById(userId);

    // 3. Gig 준비 (기존 업데이트 또는 새로 생성)
    Gig gig;
    if (gigDto.getGigId() != null) {
      // 기존 Gig 업데이트
      gig = findAndValidateUserGig(userId, gigDto.getGigId());
    } else {
      // 새 Gig 생성
      gig = new Gig(user);
    }

    // 4. 카테고리 조회 및 설정 (심사 요청 시 필수)
    Category category = findCategoryById(gigDto.getCategoryId());

    // 5. Gig 필드 업데이트
    updateGigFromDto(gig, gigDto);
    gig.setCategory(category);
    gig.setStatus(GigStatus.PENDING);

    // 6. 기존 옵션 제거 및 새 옵션 추가
    if (gig.getOptions() != null) {
      gig.getOptions().clear();
    }

    if (gigDto.getOptions() != null && !gigDto.getOptions().isEmpty()) {
      addOptionsToGig(gig, gigDto);
    }

    // 7. 저장 및 변환
    log.info("서비스 상품 심사 요청 완료. 유저 ID: {}", userId);
    return saveAndConvertToDto(gig);
  }

  /**
   * 서비스 상품 상세 정보를 조회합니다.
   *
   * @param gigId 상품 ID
   * @return 상품 상세 정보
   */
  @Transactional(readOnly = true)
  public GigDetailDto getGigDetail(Long gigId) {
    return GigDetailDto.builder().build();
  }

  /**
   * 사용자의 코칭 상품 목록을 조회합니다.
   *
   * @param userId 사용자 ID
   * @param cursor 커서 (다음 페이지 시작점)
   * @param size 조회할 상품 수
   * @param state 상품 상태 필터
   * @return 커서 기반 페이지네이션된 상품 목록
   */
  @Transactional(readOnly = true)
  public CursorResponse<GigCardDto> getMyCoachingGigs(
      Long userId, String cursor, int size, String state) {
    // 1. 코칭 카테고리 ID 조회
    List<Long> coachingCategoryIds = List.of(1L, 2L, 3L);

    // 2. 커서 디코딩
    Long lastGigId = parseGigIdFromCursor(cursor);

    // 3. 상태 필터 적용
    GigStatus gigStatus = parseGigStatus(state);

    // 4. 리포지토리 조회
    CursorResponse<FlatPreviewGigDTO> flatDtos =
        gigCustomRepository.findMyCoachingGigsWithCursor(
            userId, lastGigId, size, coachingCategoryIds, gigStatus);

    // 5. FlatPreviewGigDTO를 GigCardDto로 변환
    List<GigCardDto> cardDtos =
        flatDtos.getItems().stream()
            .map(this::convertFlatDtoToCardDto)
            .collect(Collectors.toList());

    // 6. 전체 개수 조회
    int totalCount =
        gigCustomRepository.countMyCoachingGigs(userId, coachingCategoryIds, gigStatus);

    // 7. 응답 구성
    return CursorResponse.<GigCardDto>builder()
        .items(cardDtos)
        .nextCursor(flatDtos.getNextCursor())
        .hasNext(flatDtos.isHasNext())
        .totalCount(totalCount)
        .meta(flatDtos.getMeta())
        .build();
  }

  // --- 유틸리티 메서드 ---

  /** Gig를 저장하고 DTO로 변환합니다. */
  private GigDto saveAndConvertToDto(Gig gig) {
    gig = gigRepository.save(gig);
    log.info("서비스 상품 저장 완료. ID: {}, 유저 ID: {}", gig.getId(), gig.getUser().getId());
    return convertToDto(gig);
  }

  /** 사용자의 Gig를 찾고 접근 권한을 검증합니다. */
  private Gig findAndValidateUserGig(Long userId, Long gigId) {
    Gig gig =
        gigRepository
            .findById(gigId)
            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + gigId));

    // 권한 확인 - 상품의 소유자가 현재 사용자인지 검증
    if (!gig.getUser().getId().equals(userId)) {
      throw new ForbiddenException("해당 상품을 수정할 권한이 없습니다.");
    }

    return gig;
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

  /** DTO에서 Gig 엔티티로 데이터를 업데이트합니다. */
  private void updateGigFromDto(Gig gig, GigDto dto) {
    // 타이틀 업데이트
    if (dto.getTitle() != null) {
      gig.setTitle(dto.getTitle());
    }

    // GigType 업데이트
    if (dto.getGigType() != null) {
      try {
        gig.setGigType(GigType.valueOf(dto.getGigType()));
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 상품 유형: {}", dto.getGigType());
      }
    }

    // 썸네일 URL 업데이트
    if (dto.getThumbnailUrl() != null) {
      gig.setThumbnailUrl(dto.getThumbnailUrl());
    }

    // 카테고리 업데이트 (있는 경우에만)
    if (dto.getCategoryId() != null) {
      try {
        Category category = findCategoryById(dto.getCategoryId());
        gig.setCategory(category);
      } catch (EntityNotFoundException e) {
        log.warn("카테고리를 찾을 수 없습니다: {}", dto.getCategoryId());
      }
    }
  }

  /** 옵션을 Gig에 추가합니다. */
  private void addOptionsToGig(Gig gig, GigDto dto) {
    // dto.getOptions()가 null인 경우 빈 리스트 사용 (NPE 방지)
    List<GigDto.GigOptionDto> options =
        dto.getOptions() != null ? dto.getOptions() : Collections.emptyList();

    for (GigDto.GigOptionDto optionDto : options) {
      // null 옵션 건너뛰기
      if (optionDto == null) continue;

      // gigType이 null인 경우 기본값 설정
      if (gig.getGigType() == null) {
        log.warn("상품 유형이 지정되지 않았습니다. 기본값으로 DOCUMENT 설정");
        gig.setGigType(GigType.DOCUMENT);
      }

      // 옵션 생성 및 추가
      GigOption option = createOptionByGigType(gig.getGigType(), optionDto);
      if (option != null) {
        gig.addOption(option);
      }
    }
  }

  /** Gig 유형에 맞는 옵션을 생성합니다. */
  private GigOption createOptionByGigType(GigType gigType, GigDto.GigOptionDto optionDto) {
    GigOption option;

    if (gigType == GigType.COACHING) {
      option = createCoachingOption(optionDto);
    } else if (gigType == GigType.DOCUMENT) {
      option = createDocumentOption(optionDto);
    } else {
      log.warn("지원하지 않는 상품 유형입니다: {}", gigType);
      return null;
    }

    // 공통 필드 설정
    option.setName(optionDto.getName());
    option.setDescription(optionDto.getDescription());
    option.setPrice(optionDto.getPrice());

    return option;
  }

  /** 코칭 옵션을 생성합니다. */
  private CoachingOption createCoachingOption(GigDto.GigOptionDto optionDto) {
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
  private DocumentOption createDocumentOption(GigDto.GigOptionDto optionDto) {
    DocumentOption option = new DocumentOption();

    // 문서 옵션 특화 필드 설정 - null 안전하게 처리
    if (optionDto.getContentDeliveryMethod() != null) {
      try {
        option.setContentDeliveryMethod(
            ContentDeliveryMethod.valueOf(optionDto.getContentDeliveryMethod()));
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 컨텐츠 제공 방식: {}", optionDto.getContentDeliveryMethod());
      }
    }

    return option;
  }

  /** FlatPreviewGigDTO를 GigCardDto로 변환합니다. */
  private GigCardDto convertFlatDtoToCardDto(FlatPreviewGigDTO flat) {
    return GigCardDto.builder()
        .gigId(flat.getGigId())
        .createdAt(flat.getCreatedAt())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .status(flat.getStatus())
        .build();
  }

  /** 커서에서 Gig ID를 파싱합니다. */
  private Long parseGigIdFromCursor(String cursor) {
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

  /** 문자열에서 GigStatus를 파싱합니다. */
  private GigStatus parseGigStatus(String state) {
    if (state == null || state.isBlank()) {
      return null;
    }

    try {
      return GigStatus.valueOf(state.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않은 상품 상태: {}", state);
      return null;
    }
  }

  /** Gig를 DTO로 변환합니다. */
  private GigDto convertToDto(Gig gig) {
    // null 체크
    if (gig == null) {
      return null;
    }

    // 옵션 변환
    List<GigDto.GigOptionDto> optionDtos = new ArrayList<>();
    if (gig.getOptions() != null) {
      for (GigOption option : gig.getOptions()) {
        if (option == null) continue;

        GigDto.GigOptionDto.GigOptionDtoBuilder builder =
            GigDto.GigOptionDto.builder()
                .id(option.getId())
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

    // Gig DTO 구성
    GigDto.GigDtoBuilder dtoBuilder =
        GigDto.builder()
            .gigId(gig.getId())
            .title(gig.getTitle())
            .thumbnailUrl(gig.getThumbnailUrl())
            .options(optionDtos.isEmpty() ? null : optionDtos);

    // Enum을 안전하게 문자열로 변환
    if (gig.getGigType() != null) {
      dtoBuilder.gigType(gig.getGigType().name());
    }

    if (gig.getStatus() != null) {
      dtoBuilder.status(gig.getStatus().name());
    } else {
      dtoBuilder.status(GigStatus.DRAFT.name()); // 기본값
    }

    // 카테고리가 null이 아닌 경우에만 ID 설정
    if (gig.getCategory() != null) {
      dtoBuilder.categoryId(gig.getCategory().getId());
    }

    return dtoBuilder.build();
  }

  /** 심사 요청 시 필수 항목을 검증합니다. */
  private void validateGigForSubmission(GigDto gigDto) {
    List<String> missingFields = new ArrayList<>();

    // 필수 필드 검증
    if (gigDto.getTitle() == null || gigDto.getTitle().trim().isEmpty()) {
      missingFields.add("제목");
    }

    if (gigDto.getGigType() == null) {
      missingFields.add("상품 유형");
    }

    if (gigDto.getCategoryId() == null) {
      missingFields.add("카테고리");
    }

    if (gigDto.getThumbnailUrl() == null || gigDto.getThumbnailUrl().trim().isEmpty()) {
      missingFields.add("썸네일 이미지");
    }

    // 옵션 검증
    if (gigDto.getOptions() == null || gigDto.getOptions().isEmpty()) {
      missingFields.add("상품 옵션");
    } else {
      // 각 옵션별 필수 필드 검증
      for (int i = 0; i < gigDto.getOptions().size(); i++) {
        GigDto.GigOptionDto option = gigDto.getOptions().get(i);

        if (option.getName() == null || option.getName().trim().isEmpty()) {
          missingFields.add("옵션" + (i + 1) + " 이름");
        }

        if (option.getPrice() == null) {
          missingFields.add("옵션" + (i + 1) + " 가격");
        }

        // 상품 유형별 옵션 필수 필드 검증
        if (GigType.COACHING.name().equals(gigDto.getGigType())) {
          if (option.getCoachingPeriod() == null) {
            missingFields.add("옵션" + (i + 1) + " 코칭 기간");
          }
          if (option.getCoachingType() == null) {
            missingFields.add("옵션" + (i + 1) + " 코칭 방식");
          }
        } else if (GigType.DOCUMENT.name().equals(gigDto.getGigType())) {
          if (option.getContentDeliveryMethod() == null) {
            missingFields.add("옵션" + (i + 1) + " 컨텐츠 제공 방식");
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
}
