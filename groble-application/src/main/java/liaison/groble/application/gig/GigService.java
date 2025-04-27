package liaison.groble.application.gig;

import java.util.ArrayList;
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
    // 카테고리 찾기
    Category category =
        categoryRepository
            .findById(gigDraftDto.getCategoryId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "카테고리를 찾을 수 없습니다. ID: " + gigDraftDto.getCategoryId()));

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

      // 기본 필드 업데이트
      updateGigFields(gig, gigDraftDto, category);

      // 기존 옵션 삭제 (orphanRemoval=true로 설정되어 있어서 자동으로 삭제됨)
      gig.getOptions().clear();
    } else {
      // 새 드래프트 생성
      gig = createNewGig(gigDraftDto, category);
    }

    // 옵션 추가
    addOptionsToGig(gig, gigDraftDto);

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
    // 문자열에서 Enum으로 변환
    GigType gigType = GigType.valueOf(dto.getGigType());
    GigStatus status = GigStatus.valueOf(dto.getStatus() != null ? dto.getStatus() : "DRAFT");

    gig.setTitle(dto.getTitle());
    gig.setGigType(gigType);
    gig.setCategory(category);
    gig.setThumbnailUrl(dto.getThumbnailUrl());
    gig.setStatus(status);
  }

  private void addOptionsToGig(Gig gig, GigDraftDto dto) {
    for (GigDraftDto.GigOptionDto optionDto : dto.getOptions()) {
      GigOption option;

      if (gig.getGigType() == GigType.COACHING) {
        option = createCoachingOption(optionDto);
      } else if (gig.getGigType() == GigType.DOCUMENT) {
        option = createDocumentOption(optionDto);
      } else {
        throw new IllegalArgumentException("지원하지 않는 상품 유형입니다: " + gig.getGigType());
      }

      // 공통 필드 설정
      option.setName(optionDto.getName());
      option.setDescription(optionDto.getDescription());
      option.setPrice(optionDto.getPrice());

      // Gig에 옵션 추가
      gig.addOption(option);
    }
  }

  private CoachingOption createCoachingOption(GigDraftDto.GigOptionDto optionDto) {
    CoachingOption option = new CoachingOption();

    // 코칭 옵션 특화 필드 설정
    if (optionDto.getCoachingPeriod() != null) {
      option.setCoachingPeriod(CoachingPeriod.valueOf(optionDto.getCoachingPeriod()));
    }

    if (optionDto.getDocumentProvision() != null) {
      option.setDocumentProvision(DocumentProvision.valueOf(optionDto.getDocumentProvision()));
    }

    if (optionDto.getCoachingType() != null) {
      option.setCoachingType(CoachingType.valueOf(optionDto.getCoachingType()));
    }

    option.setCoachingTypeDescription(optionDto.getCoachingTypeDescription());

    return option;
  }

  private DocumentOption createDocumentOption(GigDraftDto.GigOptionDto optionDto) {
    DocumentOption option = new DocumentOption();

    // 문서 옵션 특화 필드 설정
    if (optionDto.getContentDeliveryMethod() != null) {
      option.setContentDeliveryMethod(
          ContentDeliveryMethod.valueOf(optionDto.getContentDeliveryMethod()));
    }

    return option;
  }

  private GigDraftDto convertToDto(Gig gig) {
    List<GigDraftDto.GigOptionDto> optionDtos = new ArrayList<>();

    for (GigOption option : gig.getOptions()) {
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

    return GigDraftDto.builder()
        .gigId(gig.getId())
        .title(gig.getTitle())
        .gigType(gig.getGigType().name()) // Enum을 문자열로 변환
        .categoryId(gig.getCategory().getId())
        .thumbnailUrl(gig.getThumbnailUrl())
        .status(gig.getStatus().name()) // Enum을 문자열로 변환
        .options(optionDtos)
        .build();
  }

  @Transactional(readOnly = true)
  public GigDetailDto getGigDetail(Long gigId) {

    return GigDetailDto.builder().build();
  }
}
