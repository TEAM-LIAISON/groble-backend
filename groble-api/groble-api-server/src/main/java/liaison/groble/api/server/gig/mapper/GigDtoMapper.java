package liaison.groble.api.server.gig.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.gig.request.CoachingOptionRequest;
import liaison.groble.api.model.gig.request.DocumentOptionRequest;
import liaison.groble.api.model.gig.request.GigDraftRequest;
import liaison.groble.api.model.gig.response.GigDetailResponse;
import liaison.groble.api.model.gig.response.GigDraftResponse;
import liaison.groble.application.gig.dto.GigDetailDto;
import liaison.groble.application.gig.dto.GigDraftDto;

@Component
public class GigDtoMapper {

  public GigDraftDto toServiceGigDraftDto(GigDraftRequest request) {
    if (request == null) {
      return null;
    }

    List<GigDraftDto.GigOptionDto> options = new ArrayList<>();

    // GigType에 따라 적절한 옵션 목록 생성
    String gigType = request.getGigType();
    if (gigType != null) {
      if ("COACHING".equals(gigType) && request.getCoachingOptions() != null) {
        for (CoachingOptionRequest optionRequest : request.getCoachingOptions()) {
          if (optionRequest != null) {
            GigDraftDto.GigOptionDto option = mapToCoachingOptionDto(optionRequest);
            if (option != null) {
              options.add(option);
            }
          }
        }
      } else if ("DOCUMENT".equals(gigType) && request.getDocumentOptions() != null) {
        for (DocumentOptionRequest optionRequest : request.getDocumentOptions()) {
          if (optionRequest != null) {
            GigDraftDto.GigOptionDto option = mapToDocumentOptionDto(optionRequest);
            if (option != null) {
              options.add(option);
            }
          }
        }
      }
    }

    // 모든 필드가 null일 수 있음을 고려
    GigDraftDto.GigDraftDtoBuilder builder = GigDraftDto.builder();

    // 모든 필드에 대해 null 체크 없이 그대로 설정
    builder.gigId(request.getGigId());
    builder.title(request.getTitle());
    builder.gigType(gigType);
    builder.categoryId(request.getCategoryId());
    builder.thumbnailUrl(request.getThumbnailUrl());

    // 상태만 항상 DRAFT로 설정
    builder.status("DRAFT");

    // 옵션 설정
    builder.options(options.isEmpty() ? null : options);

    return builder.build();
  }

  private GigDraftDto.GigOptionDto mapToCoachingOptionDto(CoachingOptionRequest optionRequest) {
    if (optionRequest == null) {
      return null;
    }

    // 모든 필드가 null일 수 있음
    return GigDraftDto.GigOptionDto.builder()
        .id(null) // 새로 생성되는 옵션은 ID가 없음
        .name(optionRequest.getName())
        .description(optionRequest.getDescription())
        .price(optionRequest.getPrice())
        .coachingPeriod(optionRequest.getCoachingPeriod())
        .documentProvision(optionRequest.getDocumentProvision())
        .coachingType(optionRequest.getCoachingType())
        .coachingTypeDescription(optionRequest.getCoachingTypeDescription())
        .build();
  }

  private GigDraftDto.GigOptionDto mapToDocumentOptionDto(DocumentOptionRequest optionRequest) {
    if (optionRequest == null) {
      return null;
    }

    // 모든 필드가 null일 수 있음
    return GigDraftDto.GigOptionDto.builder()
        .id(null) // 새로 생성되는 옵션은 ID가 없음
        .name(optionRequest.getName())
        .description(optionRequest.getDescription())
        .price(optionRequest.getPrice())
        .contentDeliveryMethod(optionRequest.getContentDeliveryMethod())
        .build();
  }

  public GigDraftResponse toGigDraftResponse(GigDraftDto dto) {
    if (dto == null) {
      return null;
    }

    List<GigDraftResponse.OptionResponse> optionResponses = new ArrayList<>();

    if (dto.getOptions() != null) {
      for (GigDraftDto.GigOptionDto optionDto : dto.getOptions()) {
        if (optionDto != null) {
          // 모든 필드가 null일 수 있음을 고려
          GigDraftResponse.OptionResponse.OptionResponseBuilder optionBuilder =
              GigDraftResponse.OptionResponse.builder();

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
    GigDraftResponse.GigDraftResponseBuilder responseBuilder = GigDraftResponse.builder();

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
}
