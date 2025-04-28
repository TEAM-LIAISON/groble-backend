package liaison.groble.api.server.gig.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.gig.request.draft.CoachingOptionDraftRequest;
import liaison.groble.api.model.gig.request.draft.DocumentOptionDraftRequest;
import liaison.groble.api.model.gig.request.draft.GigDraftRequest;
import liaison.groble.api.model.gig.request.register.CoachingOptionRegisterRequest;
import liaison.groble.api.model.gig.request.register.DocumentOptionRegisterRequest;
import liaison.groble.api.model.gig.request.register.GigRegisterRequest;
import liaison.groble.api.model.gig.response.GigDetailResponse;
import liaison.groble.api.model.gig.response.GigPreviewCardResponse;
import liaison.groble.api.model.gig.response.GigResponse;
import liaison.groble.application.gig.dto.GigCardDto;
import liaison.groble.application.gig.dto.GigDetailDto;
import liaison.groble.application.gig.dto.GigDto;

@Component
public class GigDtoMapper {

  /** GigDraftRequest를 GigDto로 변환 (임시 저장용) - 모든 필드가 null일 수 있음 */
  public GigDto toServiceGigDtoFromDraft(GigDraftRequest request) {
    if (request == null) {
      return null;
    }

    List<GigDto.GigOptionDto> options = new ArrayList<>();

    // GigType에 따라 적절한 옵션 목록 생성
    String gigType = request.getGigType();
    if (gigType != null) {
      if ("COACHING".equals(gigType) && request.getCoachingOptions() != null) {
        for (CoachingOptionDraftRequest optionRequest : request.getCoachingOptions()) {
          if (optionRequest != null) {
            GigDto.GigOptionDto option = mapToCoachingOptionDto(optionRequest);
            if (option != null) {
              options.add(option);
            }
          }
        }
      } else if ("DOCUMENT".equals(gigType) && request.getDocumentOptions() != null) {
        for (DocumentOptionDraftRequest optionRequest : request.getDocumentOptions()) {
          if (optionRequest != null) {
            GigDto.GigOptionDto option = mapToDocumentOptionDto(optionRequest);
            if (option != null) {
              options.add(option);
            }
          }
        }
      }
    }

    // 모든 필드가 null일 수 있음을 고려
    GigDto.GigDtoBuilder builder = GigDto.builder();

    // 모든 필드에 대해 null 체크 없이 그대로 설정
    builder.gigId(request.getGigId());
    builder.title(request.getTitle());
    builder.gigType(gigType);
    builder.categoryId(request.getCategoryId());
    builder.thumbnailUrl(request.getThumbnailUrl());

    // 임시저장 상태 설정
    builder.status("DRAFT");

    // 옵션 설정
    builder.options(options.isEmpty() ? null : options);

    return builder.build();
  }

  /** GigRegisterRequest를 GigDto로 변환 (등록용) - 필수 필드에 대한 검증 수행 */
  public GigDto toServiceGigDtoFromRegister(GigRegisterRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("상품 등록 요청이 null입니다.");
    }

    // 필수 필드 검증
    validateRegisterRequest(request);

    List<GigDto.GigOptionDto> options = new ArrayList<>();

    // GigType에 따라 적절한 옵션 목록 생성
    String gigType = request.getGigType();
    if ("COACHING".equals(gigType) && request.getCoachingOptions() != null) {
      for (CoachingOptionRegisterRequest optionRequest : request.getCoachingOptions()) {
        validateCoachingOptionRequest(optionRequest);
        options.add(mapToCoachingOptionDto(optionRequest));
      }
    } else if ("DOCUMENT".equals(gigType) && request.getDocumentOptions() != null) {
      for (DocumentOptionRegisterRequest optionRequest : request.getDocumentOptions()) {
        validateDocumentOptionRequest(optionRequest);
        options.add(mapToDocumentOptionDto(optionRequest));
      }
    }

    GigDto.GigDtoBuilder builder = GigDto.builder();

    builder.gigId(request.getGigId());
    builder.title(request.getTitle());
    builder.gigType(gigType);
    builder.categoryId(request.getCategoryId());
    builder.thumbnailUrl(request.getThumbnailUrl());

    // 등록 요청 상태 설정
    builder.status("PENDING_REVIEW");

    // 옵션 설정 - 등록 시에는 최소 1개 이상의 옵션이 필요
    builder.options(options);

    return builder.build();
  }

  /** 등록 요청의 필수 필드 검증 */
  private void validateRegisterRequest(GigRegisterRequest request) {
    if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("상품명은 필수 입력 항목입니다.");
    }

    if (request.getGigType() == null) {
      throw new IllegalArgumentException("상품 유형은 필수 입력 항목입니다.");
    }

    if (!("COACHING".equals(request.getGigType()) || "DOCUMENT".equals(request.getGigType()))) {
      throw new IllegalArgumentException("유효하지 않은 상품 유형입니다: " + request.getGigType());
    }

    if (request.getCategoryId() == null) {
      throw new IllegalArgumentException("카테고리는 필수 입력 항목입니다.");
    }

    if (request.getThumbnailUrl() == null || request.getThumbnailUrl().trim().isEmpty()) {
      throw new IllegalArgumentException("썸네일 이미지는 필수 입력 항목입니다.");
    }

    // 옵션 검증
    if ("COACHING".equals(request.getGigType())) {
      if (request.getCoachingOptions() == null || request.getCoachingOptions().isEmpty()) {
        throw new IllegalArgumentException("코칭 상품은 최소 1개 이상의 옵션이 필요합니다.");
      }
    } else {
      if (request.getDocumentOptions() == null || request.getDocumentOptions().isEmpty()) {
        throw new IllegalArgumentException("문서 상품은 최소 1개 이상의 옵션이 필요합니다.");
      }
    }
  }

  /** 코칭 옵션 요청의 필수 필드 검증 */
  private void validateCoachingOptionRequest(CoachingOptionRegisterRequest option) {
    if (option == null) {
      throw new IllegalArgumentException("옵션 정보가 null입니다.");
    }

    if (option.getName() == null || option.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("옵션명은 필수 입력 항목입니다.");
    }

    if (option.getDescription() == null || option.getDescription().trim().isEmpty()) {
      throw new IllegalArgumentException("옵션 설명은 필수 입력 항목입니다.");
    }

    if (option.getPrice() == null) {
      throw new IllegalArgumentException("가격은 필수 입력 항목입니다.");
    }

    if (option.getCoachingPeriod() == null) {
      throw new IllegalArgumentException("코칭 기간은 필수 입력 항목입니다.");
    }

    if (option.getCoachingType() == null) {
      throw new IllegalArgumentException("코칭 방식은 필수 입력 항목입니다.");
    }
  }

  /** 문서 옵션 요청의 필수 필드 검증 */
  private void validateDocumentOptionRequest(DocumentOptionRegisterRequest option) {
    if (option == null) {
      throw new IllegalArgumentException("옵션 정보가 null입니다.");
    }

    if (option.getName() == null || option.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("옵션명은 필수 입력 항목입니다.");
    }

    if (option.getDescription() == null || option.getDescription().trim().isEmpty()) {
      throw new IllegalArgumentException("옵션 설명은 필수 입력 항목입니다.");
    }

    if (option.getPrice() == null) {
      throw new IllegalArgumentException("가격은 필수 입력 항목입니다.");
    }

    if (option.getContentDeliveryMethod() == null) {
      throw new IllegalArgumentException("컨텐츠 제공 방식은 필수 입력 항목입니다.");
    }
  }

  // T 타입은 특정 조건을 만족해야 함
  private <T> GigDto.GigOptionDto mapToCoachingOptionDto(T optionRequest) {
    if (optionRequest == null) {
      return null;
    }

    // 타입 검사 및 캐스팅
    if (!(optionRequest instanceof CoachingOptionDraftRequest)
        && !(optionRequest instanceof CoachingOptionRegisterRequest)) {
      throw new IllegalArgumentException("지원하지 않는 옵션 요청 타입입니다.");
    }

    // getter 메서드 접근
    String name = null;
    String description = null;
    java.math.BigDecimal price = null;
    String coachingPeriod = null;
    String documentProvision = null;
    String coachingType = null;
    String coachingTypeDescription = null;

    if (optionRequest instanceof CoachingOptionDraftRequest) {
      CoachingOptionDraftRequest request = (CoachingOptionDraftRequest) optionRequest;
      name = request.getName();
      description = request.getDescription();
      price = request.getPrice();
      coachingPeriod = request.getCoachingPeriod();
      documentProvision = request.getDocumentProvision();
      coachingType = request.getCoachingType();
      coachingTypeDescription = request.getCoachingTypeDescription();
    } else {
      CoachingOptionRegisterRequest request = (CoachingOptionRegisterRequest) optionRequest;
      name = request.getName();
      description = request.getDescription();
      price = request.getPrice();
      coachingPeriod = request.getCoachingPeriod();
      documentProvision = request.getDocumentProvision();
      coachingType = request.getCoachingType();
      coachingTypeDescription = request.getCoachingTypeDescription();
    }

    return GigDto.GigOptionDto.builder()
        .id(null)
        .name(name)
        .description(description)
        .price(price)
        .coachingPeriod(coachingPeriod)
        .documentProvision(documentProvision)
        .coachingType(coachingType)
        .coachingTypeDescription(coachingTypeDescription)
        .build();
  }

  private <T> GigDto.GigOptionDto mapToDocumentOptionDto(T optionRequest) {
    if (optionRequest == null) {
      return null;
    }

    // 지원하는 타입인지 확인
    if (!(optionRequest instanceof DocumentOptionDraftRequest)
        && !(optionRequest instanceof DocumentOptionRegisterRequest)) {
      throw new IllegalArgumentException(
          "지원하지 않는 문서 옵션 요청 타입입니다: " + optionRequest.getClass().getName());
    }

    // 기본 정보 추출
    String name = null;
    String description = null;
    java.math.BigDecimal price = null;
    String contentDeliveryMethod = null;

    // 타입에 따라 데이터 추출
    if (optionRequest instanceof DocumentOptionDraftRequest) {
      DocumentOptionDraftRequest request = (DocumentOptionDraftRequest) optionRequest;
      name = request.getName();
      description = request.getDescription();
      price = request.getPrice();
      contentDeliveryMethod = request.getContentDeliveryMethod();
    } else {
      DocumentOptionRegisterRequest request = (DocumentOptionRegisterRequest) optionRequest;
      name = request.getName();
      description = request.getDescription();
      price = request.getPrice();
      contentDeliveryMethod = request.getContentDeliveryMethod();

      // Register 타입에 대한 추가 유효성 검사
      if (name == null || price == null || contentDeliveryMethod == null) {
        throw new IllegalArgumentException("등록 시 옵션명, 가격, 컨텐츠 제공 방식은 필수 입력 항목입니다.");
      }
    }

    // DTO 생성 및 반환
    return GigDto.GigOptionDto.builder()
        .id(null) // 새로 생성되는 옵션은 ID가 없음
        .name(name)
        .description(description)
        .price(price)
        .contentDeliveryMethod(contentDeliveryMethod)
        .build();
  }

  public GigResponse toGigDraftResponse(GigDto dto) {
    if (dto == null) {
      return null;
    }

    List<GigResponse.OptionResponse> optionResponses = new ArrayList<>();

    if (dto.getOptions() != null) {
      for (GigDto.GigOptionDto optionDto : dto.getOptions()) {
        if (optionDto != null) {
          // 모든 필드가 null일 수 있음을 고려
          GigResponse.OptionResponse.OptionResponseBuilder optionBuilder =
              GigResponse.OptionResponse.builder();

          // 각 필드를 그대로 설정 (null이어도 괜찮음)
          optionBuilder.id(optionDto.getId());
          optionBuilder.name(optionDto.getName());
          optionBuilder.description(optionDto.getDescription());
          optionBuilder.price(optionDto.getPrice());
          optionBuilder.coachingPeriod(optionDto.getCoachingPeriod());
          optionBuilder.documentProvision(optionDto.getDocumentProvision());
          optionBuilder.coachingType(optionDto.getCoachingType());
          optionBuilder.coachingTypeDescription(optionDto.getCoachingTypeDescription());
          optionBuilder.contentDeliveryMethod(optionDto.getContentDeliveryMethod());

          optionResponses.add(optionBuilder.build());
        }
      }
    }

    // 모든 필드가 null일 수 있음을 고려
    GigResponse.GigResponseBuilder responseBuilder = GigResponse.builder();

    // 각 필드를 그대로 설정 (null이어도 괜찮음)
    responseBuilder.id(dto.getGigId());
    responseBuilder.title(dto.getTitle());
    responseBuilder.gigType(dto.getGigType());
    responseBuilder.categoryId(dto.getCategoryId());
    responseBuilder.thumbnailUrl(dto.getThumbnailUrl());
    responseBuilder.status(dto.getStatus());

    // 옵션이 없는 경우 null로 설정 (빈 리스트가 아닌)
    responseBuilder.options(optionResponses.isEmpty() ? null : optionResponses);

    return responseBuilder.build();
  }

  public GigDetailResponse toGigDetailResponse(GigDetailDto gigDetailDto) {
    // 기존 메서드 구현 유지
    return GigDetailResponse.builder().build();
  }

  public GigPreviewCardResponse toGigPreviewCardFromCardDto(GigCardDto cardDto) {
    return GigPreviewCardResponse.builder()
        .gigId(cardDto.getGigId())
        .createdAt(cardDto.getCreatedAt())
        .title(cardDto.getTitle())
        .thumbnailUrl(cardDto.getThumbnailUrl())
        .sellerName(cardDto.getSellerName())
        .status(cardDto.getStatus())
        .build();
  }
}
