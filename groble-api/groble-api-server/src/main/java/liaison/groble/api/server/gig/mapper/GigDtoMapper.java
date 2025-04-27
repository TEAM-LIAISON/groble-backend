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
    List<GigDraftDto.GigOptionDto> options = new ArrayList<>();

    // GigType에 따라 적절한 옵션 목록 생성
    if (request.getGigType().equals("COACHING") && request.getCoachingOptions() != null) {
      for (CoachingOptionRequest optionRequest : request.getCoachingOptions()) {
        options.add(mapToCoachingOptionDto(optionRequest));
      }
    } else if (request.getGigType().equals("DOCUMENT") && request.getDocumentOptions() != null) {
      for (DocumentOptionRequest optionRequest : request.getDocumentOptions()) {
        options.add(mapToDocumentOptionDto(optionRequest));
      }
    }

    return GigDraftDto.builder()
        .gigId(request.getGigId())
        .title(request.getTitle())
        .gigType(request.getGigType()) // Enum을 문자열로 전달
        .categoryId(request.getCategoryId())
        .thumbnailUrl(request.getThumbnailUrl())
        .status("DRAFT") // 상태 문자열로 전달
        .options(options)
        .build();
  }

  private GigDraftDto.GigOptionDto mapToCoachingOptionDto(CoachingOptionRequest optionRequest) {
    return GigDraftDto.GigOptionDto.builder()
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
    return GigDraftDto.GigOptionDto.builder()
        .name(optionRequest.getName())
        .description(optionRequest.getDescription())
        .price(optionRequest.getPrice())
        .contentDeliveryMethod(optionRequest.getContentDeliveryMethod())
        .build();
  }

  public GigDraftResponse toGigDraftResponse(GigDraftDto dto) {
    List<GigDraftResponse.OptionResponse> optionResponses = new ArrayList<>();

    if (dto.getOptions() != null) {
      for (GigDraftDto.GigOptionDto optionDto : dto.getOptions()) {
        optionResponses.add(
            GigDraftResponse.OptionResponse.builder()
                .id(optionDto.getId())
                .name(optionDto.getName())
                .description(optionDto.getDescription())
                .price(optionDto.getPrice())
                .coachingPeriod(optionDto.getCoachingPeriod())
                .documentProvision(optionDto.getDocumentProvision())
                .coachingType(optionDto.getCoachingType())
                .coachingTypeDescription(optionDto.getCoachingTypeDescription())
                .contentDeliveryMethod(optionDto.getContentDeliveryMethod())
                .build());
      }
    }

    return GigDraftResponse.builder()
        .id(dto.getGigId())
        .title(dto.getTitle())
        .gigType(dto.getGigType()) // 문자열로 전달
        .categoryId(dto.getCategoryId())
        .thumbnailUrl(dto.getThumbnailUrl())
        .status(dto.getStatus()) // 문자열로 전달
        .options(optionResponses)
        .build();
  }

  public GigDetailResponse toGigDetailResponse(GigDetailDto gigDetailDto) {
    // 기존 메서드 구현 유지
    return GigDetailResponse.builder().build();
  }
}
