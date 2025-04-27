package liaison.groble.application.gig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.gig.dto.GigDetailDto;
import liaison.groble.application.gig.dto.GigDraftDto;
import liaison.groble.common.exception.EntityNotFoundException;
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
import liaison.groble.domain.gig.repository.GigRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GigService {
  private final GigRepository gigRepository;
  private final CategoryRepository categoryRepository;

  @Transactional
  public GigDraftDto saveDraftAndReturn(Long userId, GigDraftDto gigDraftDto) {
    // null 체크 - 입력 DTO가 null인 경우 빈 응답 반환
    if (gigDraftDto == null) {
      log.warn("임시 저장 요청에 데이터가 없습니다.");
      Gig emptyGig = new Gig();
      emptyGig.setStatus(GigStatus.DRAFT);
      emptyGig = gigRepository.save(emptyGig);
      return convertToDto(emptyGig);
    }

    // 카테고리 ID가 null이 아닌 경우에만 카테고리 조회
    Category category = null;
    if (gigDraftDto.getCategoryId() != null) {
      category =
          categoryRepository
              .findById(gigDraftDto.getCategoryId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "카테고리를 찾을 수 없습니다. ID: " + gigDraftDto.getCategoryId()));
    }

    // 기존 드래프트 있는지 확인
    Gig gig;
    if (gigDraftDto.getGigId() != null) {
      // 기존 드래프트 업데이트
      gig =
          gigRepository
              .findById(gigDraftDto.getGigId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + gigDraftDto.getGigId()));

      // 권한 확인 로직은 필요에 따라 추가

      // 기본 필드 업데이트 - 카테고리가 null일 수 있음을 고려
      updateGigFields(gig, gigDraftDto, category);

      // 기존 옵션 삭제 - options가 null이 아닌 경우에만 처리
      if (gig.getOptions() != null) {
        gig.getOptions().clear();
      }
    } else {
      // 새 드래프트 생성
      gig = createNewGig(gigDraftDto, category);
    }

    // 옵션 추가 - 옵션이 있는 경우에만 처리
    if (gigDraftDto.getOptions() != null && !gigDraftDto.getOptions().isEmpty()) {
      addOptionsToGig(gig, gigDraftDto);
    }

    // 저장
    gig = gigRepository.save(gig);

    log.info("서비스 상품 임시 저장 완료. ID: {}, 유저 ID: {}", gig.getId(), userId);

    // 저장된 데이터로 바로 응답 DTO 생성
    return convertToDto(gig);
  }

  private Gig createNewGig(GigDraftDto dto, Category category) {
    Gig gig = new Gig();
    updateGigFields(gig, dto, category);
    return gig;
  }

  private void updateGigFields(Gig gig, GigDraftDto dto, Category category) {
    // 문자열에서 Enum으로 변환 - null 체크 추가
    GigType gigType = null;
    if (dto.getGigType() != null) {
      try {
        gigType = GigType.valueOf(dto.getGigType());
      } catch (IllegalArgumentException e) {
        log.warn("유효하지 않은 상품 유형: {}", dto.getGigType());
        // 기본값 설정 또는 그대로 null로 유지할 수 있음
      }
    }

    // 상태는 항상 DRAFT로 설정
    GigStatus status = GigStatus.DRAFT;

    // 필드 업데이트 - null 허용
    gig.setTitle(dto.getTitle());
    gig.setGigType(gigType);
    gig.setCategory(category); // null일 수 있음
    gig.setThumbnailUrl(dto.getThumbnailUrl());
    gig.setStatus(status);
  }

  private void addOptionsToGig(Gig gig, GigDraftDto dto) {
    // dto.getOptions()가 null인 경우 빈 리스트 반환 (NPE 방지)
    List<GigDraftDto.GigOptionDto> options =
        dto.getOptions() != null ? dto.getOptions() : Collections.emptyList();

    for (GigDraftDto.GigOptionDto optionDto : options) {
      // null 옵션 건너뛰기
      if (optionDto == null) continue;

      GigOption option;

      // gigType이 null인 경우 기본값으로 설정하거나 예외 처리
      if (gig.getGigType() == null) {
        log.warn("상품 유형이 지정되지 않았습니다. 기본값으로 DOCUMENT 설정");
        gig.setGigType(GigType.DOCUMENT);
      }

      // 유형에 따라 옵션 생성
      if (gig.getGigType() == GigType.COACHING) {
        option = createCoachingOption(optionDto);
      } else if (gig.getGigType() == GigType.DOCUMENT) {
        option = createDocumentOption(optionDto);
      } else {
        log.warn("지원하지 않는 상품 유형입니다: {}", gig.getGigType());
        continue; // 지원하지 않는 유형은 건너뛰기
      }

      // 공통 필드 설정 - null 허용
      option.setName(optionDto.getName());
      option.setDescription(optionDto.getDescription());
      option.setPrice(optionDto.getPrice());

      // Gig에 옵션 추가
      gig.addOption(option);
    }
  }

  private CoachingOption createCoachingOption(GigDraftDto.GigOptionDto optionDto) {
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

  private DocumentOption createDocumentOption(GigDraftDto.GigOptionDto optionDto) {
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

  private GigDraftDto convertToDto(Gig gig) {
    // null 체크
    if (gig == null) {
      return null;
    }

    List<GigDraftDto.GigOptionDto> optionDtos = new ArrayList<>();

    // 옵션 목록이 null이 아닐 때만 처리
    if (gig.getOptions() != null) {
      for (GigOption option : gig.getOptions()) {
        // 옵션이 null이면 건너뛰기
        if (option == null) continue;

        GigDraftDto.GigOptionDto.GigOptionDtoBuilder builder =
            GigDraftDto.GigOptionDto.builder()
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

    GigDraftDto.GigDraftDtoBuilder dtoBuilder =
        GigDraftDto.builder()
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
      dtoBuilder.status("DRAFT"); // 기본값
    }

    // 카테고리가 null이 아닌 경우에만 ID 설정
    if (gig.getCategory() != null) {
      dtoBuilder.categoryId(gig.getCategory().getId());
    }

    return dtoBuilder.build();
  }

  @Transactional(readOnly = true)
  public GigDetailDto getGigDetail(Long gigId) {
    return GigDetailDto.builder().build();
  }
}
