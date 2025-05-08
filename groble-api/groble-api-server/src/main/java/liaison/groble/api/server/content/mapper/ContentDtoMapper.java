package liaison.groble.api.server.content.mapper;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;
import liaison.groble.api.model.content.response.BaseOptionResponse;
import liaison.groble.api.model.content.response.CoachingOptionResponse;
import liaison.groble.api.model.content.response.DocumentOptionResponse;
import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.request.draft.CoachingOptionDraftRequest;
import liaison.groble.api.model.content.request.draft.ContentDraftRequest;
import liaison.groble.api.model.content.request.draft.DocumentOptionDraftRequest;
import liaison.groble.api.model.content.request.register.CoachingOptionRegisterRequest;
import liaison.groble.api.model.content.request.register.ContentRegisterRequest;
import liaison.groble.api.model.content.request.register.DocumentOptionRegisterRequest;
import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.ContentResponse;
import liaison.groble.api.model.content.response.ContentStatusResponse;
import liaison.groble.application.content.dto.ContentCardDto;
import liaison.groble.application.content.dto.ContentDetailDto;
import liaison.groble.application.content.dto.ContentDto;
import liaison.groble.application.content.dto.ContentOptionDto;

@Component
public class ContentDtoMapper {

  /** ContentDraftRequest를 ContentDto로 변환 (임시 저장용) - 모든 필드가 null일 수 있음 */
  public ContentDto toServiceContentDtoFromDraft(ContentDraftRequest request) {
    if (request == null) {
      return null;
    }

    List<ContentOptionDto> options = new ArrayList<>();

    // ContentType에 따라 적절한 옵션 목록 생성
    String contentType = request.getContentType();
    if (contentType != null) {
      if ("COACHING".equals(contentType) && request.getCoachingOptions() != null) {
        for (CoachingOptionDraftRequest optionRequest : request.getCoachingOptions()) {
          if (optionRequest != null) {
            ContentOptionDto option = mapToCoachingOptionDto(optionRequest);
            if (option != null) {
              options.add(option);
            }
          }
        }
      } else if ("DOCUMENT".equals(contentType) && request.getDocumentOptions() != null) {
        for (DocumentOptionDraftRequest optionRequest : request.getDocumentOptions()) {
          if (optionRequest != null) {
            ContentOptionDto option = mapToDocumentOptionDto(optionRequest);
            if (option != null) {
              options.add(option);
            }
          }
        }
      }
    }

    // 모든 필드가 null일 수 있음을 고려
    ContentDto.ContentDtoBuilder builder = ContentDto.builder();

    // 모든 필드에 대해 null 체크 없이 그대로 설정
    builder.contentId(request.getContentId());
    builder.title(request.getTitle());
    builder.contentType(contentType);
    builder.categoryId(request.getCategoryId());
    builder.thumbnailUrl(request.getThumbnailUrl());

    // 임시저장 상태 설정
    builder.status("DRAFT");

    // 옵션 설정
    builder.options(options.isEmpty() ? null : options);

    return builder.build();
  }

  /** ContentRegisterRequest를 ContentDto로 변환 (등록용) - 필수 필드에 대한 검증 수행 */
  public ContentDto toServiceContentDtoFromRegister(ContentRegisterRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("콘텐츠 심사 요청이 null입니다.");
    }

    // 필수 필드 검증
    validateRegisterRequest(request);

    List<ContentOptionDto> options = new ArrayList<>();

    // ContentType에 따라 적절한 옵션 목록 생성
    String contentType = request.getContentType();
    if ("COACHING".equals(contentType) && request.getCoachingOptions() != null) {
      for (CoachingOptionRegisterRequest optionRequest : request.getCoachingOptions()) {
        validateCoachingOptionRequest(optionRequest);
        options.add(mapToCoachingOptionDto(optionRequest));
      }
    } else if ("DOCUMENT".equals(contentType) && request.getDocumentOptions() != null) {
      for (DocumentOptionRegisterRequest optionRequest : request.getDocumentOptions()) {
        validateDocumentOptionRequest(optionRequest);
        options.add(mapToDocumentOptionDto(optionRequest));
      }
    }

    ContentDto.ContentDtoBuilder builder = ContentDto.builder();

    builder.contentId(request.getContentId());
    builder.title(request.getTitle());
    builder.contentType(contentType);
    builder.categoryId(request.getCategoryId());
    builder.thumbnailUrl(request.getThumbnailUrl());

    // 등록 요청 상태 설정
    builder.status("PENDING_REVIEW");

    // 옵션 설정 - 등록 시에는 최소 1개 이상의 옵션이 필요
    builder.options(options);

    return builder.build();
  }

  /** 등록 요청의 필수 필드 검증 */
  private void validateRegisterRequest(ContentRegisterRequest request) {
    if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("콘텐츠 이름은 필수 입력 항목입니다.");
    }

    if (request.getContentType() == null) {
      throw new IllegalArgumentException("콘텐츠 유형은 필수 입력 항목입니다.");
    }

    if (!("COACHING".equals(request.getContentType())
        || "DOCUMENT".equals(request.getContentType()))) {
      throw new IllegalArgumentException("유효하지 않은 콘텐츠 유형입니다: " + request.getContentType());
    }

    if (request.getCategoryId() == null) {
      throw new IllegalArgumentException("카테고리는 필수 입력 항목입니다.");
    }

    if (request.getThumbnailUrl() == null || request.getThumbnailUrl().trim().isEmpty()) {
      throw new IllegalArgumentException("썸네일 이미지는 필수 입력 항목입니다.");
    }

    // 옵션 검증
    if ("COACHING".equals(request.getContentType())) {
      if (request.getCoachingOptions() == null || request.getCoachingOptions().isEmpty()) {
        throw new IllegalArgumentException("코칭 콘텐츠는 최소 1개 이상의 옵션이 필요합니다.");
      }
    } else {
      if (request.getDocumentOptions() == null || request.getDocumentOptions().isEmpty()) {
        throw new IllegalArgumentException("문서 콘텐츠는 최소 1개 이상의 옵션이 필요합니다.");
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
      throw new IllegalArgumentException("콘텐츠 제공 방식은 필수 입력 항목입니다.");
    }
  }

  // T 타입은 특정 조건을 만족해야 함
  private <T> ContentOptionDto mapToCoachingOptionDto(T optionRequest) {
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

    return ContentOptionDto.builder()
        .contentOptionId(null)
        .name(name)
        .description(description)
        .price(price)
        .coachingPeriod(coachingPeriod)
        .documentProvision(documentProvision)
        .coachingType(coachingType)
        .coachingTypeDescription(coachingTypeDescription)
        .build();
  }

  private <T> ContentOptionDto mapToDocumentOptionDto(T optionRequest) {
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
        throw new IllegalArgumentException("등록 시 옵션명, 가격, 콘텐츠 제공 방식은 필수 입력 항목입니다.");
      }
    }

    // DTO 생성 및 반환
    return ContentOptionDto.builder()
        .contentOptionId(null) // 새로 생성되는 옵션은 ID가 없음
        .name(name)
        .description(description)
        .price(price)
        .contentDeliveryMethod(contentDeliveryMethod)
        .build();
  }

  public ContentResponse toContentDraftResponse(ContentDto dto) {
    if (dto == null) {
      return null;
    }

    List<ContentResponse.OptionResponse> optionResponses = new ArrayList<>();

    if (dto.getOptions() != null) {
      for (ContentOptionDto optionDto : dto.getOptions()) {
        if (optionDto != null) {
          // 모든 필드가 null일 수 있음을 고려
          ContentResponse.OptionResponse.OptionResponseBuilder optionBuilder =
              ContentResponse.OptionResponse.builder();

          // 각 필드를 그대로 설정 (null이어도 괜찮음)
          optionBuilder.id(optionDto.getContentOptionId());
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
    ContentResponse.ContentResponseBuilder responseBuilder = ContentResponse.builder();

    // 각 필드를 그대로 설정 (null이어도 괜찮음)
    responseBuilder.id(dto.getContentId());
    responseBuilder.title(dto.getTitle());
    responseBuilder.contentType(dto.getContentType());
    responseBuilder.categoryId(dto.getCategoryId());
    responseBuilder.thumbnailUrl(dto.getThumbnailUrl());
    responseBuilder.status(dto.getStatus());

    // 옵션이 없는 경우 null로 설정 (빈 리스트가 아닌)
    responseBuilder.options(optionResponses.isEmpty() ? null : optionResponses);

    return responseBuilder.build();
  }

    public ContentDetailResponse toContentDetailResponse(ContentDetailDto contentDetailDto) {
        List<BaseOptionResponse> optionResponses = contentDetailDto.getOptions().stream()
                .map(optionDto -> {
                    // 코칭 옵션인 경우
                    if (optionDto.getCoachingPeriod() != null) {
                        return CoachingOptionResponse.builder()
                                .optionId(optionDto.getContentOptionId())
                                .name(optionDto.getName())
                                .description(optionDto.getDescription())
                                .price(optionDto.getPrice())
                                .coachingPeriod(optionDto.getCoachingPeriod())
                                .documentProvision(optionDto.getDocumentProvision())
                                .coachingType(optionDto.getCoachingType())
                                .coachingTypeDescription(optionDto.getCoachingTypeDescription())
                                .build();
                    }
                    // 문서 옵션인 경우
                    else if (optionDto.getContentDeliveryMethod() != null) {
                        return DocumentOptionResponse.builder()
                                .optionId(optionDto.getContentOptionId())
                                .name(optionDto.getName())
                                .description(optionDto.getDescription())
                                .price(optionDto.getPrice())
                                .contentDeliveryMethod(optionDto.getContentDeliveryMethod())
                                .build();
                    }
                    // 기본 옵션의 경우
                    else {
                        return BaseOptionResponse.builder()
                                .optionId(optionDto.getContentOptionId())
                                .name(optionDto.getName())
                                .description(optionDto.getDescription())
                                .price(optionDto.getPrice())
                                .build();
                    }
                })
                .collect(Collectors.toList());

        // 나머지 코드는 동일
        return ContentDetailResponse.builder()
                .contentId(contentDetailDto.getContentId())
                .status(contentDetailDto.getStatus())
                .contentsImageUrls(contentDetailDto.getContentsImageUrls())
                .contentType(contentDetailDto.getContentType())
                .categoryId(contentDetailDto.getCategoryId())
                .title(contentDetailDto.getTitle())
                .sellerProfileImageUrl(contentDetailDto.getSellerProfileImageUrl())
                .sellerName(contentDetailDto.getSellerName())
                .lowestPrice(contentDetailDto.getLowestPrice())
                .options(optionResponses)
                .serviceTarget(contentDetailDto.getServiceTarget())
                .serviceProcess(contentDetailDto.getServiceProcess())
                .makerIntro(contentDetailDto.getMakerIntro())
                .build();
    }

  public ContentPreviewCardResponse toContentPreviewCardFromCardDto(ContentCardDto cardDto) {
    return ContentPreviewCardResponse.builder()
        .contentId(cardDto.getContentId())
        .createdAt(cardDto.getCreatedAt())
        .title(cardDto.getTitle())
        .thumbnailUrl(cardDto.getThumbnailUrl())
        .sellerName(cardDto.getSellerName())
        .lowestPrice(cardDto.getLowestPrice())
        .status(cardDto.getStatus())
        .build();
  }

  public ContentStatusResponse toContentStatusResponse(ContentDto dto) {
    if (dto == null) {
      return null;
    }

    return ContentStatusResponse.builder()
        .contentId(dto.getContentId())
        .status(dto.getStatus())
        .build();
  }
}
