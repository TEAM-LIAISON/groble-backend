package liaison.groble.mapping.content;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import liaison.groble.api.model.content.request.draft.ContentDraftRequest;
import liaison.groble.api.model.content.request.register.ContentRegisterRequest;
import liaison.groble.api.model.content.response.BaseOptionResponse;
import liaison.groble.api.model.content.response.CoachingOptionResponse;
import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.ContentResponse;
import liaison.groble.api.model.content.response.ContentStatusResponse;
import liaison.groble.api.model.content.response.DocumentOptionResponse;
import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.dto.ContentDetailDTO;
import liaison.groble.application.content.dto.ContentOptionDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ContentMapper extends PageResponseMapper {
  // ====== 📥 Request → DTO 변환 ======
  @Mapping(target = "options", source = ".", qualifiedByName = "mapDraftOptions")
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "subscriptionSellStatus", ignore = true)
  ContentDTO toContentDTO(ContentDraftRequest contentDraftRequest);

  @Mapping(target = "options", source = ".", qualifiedByName = "mapRegisterOptions")
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "subscriptionSellStatus", ignore = true)
  ContentDTO toContentDTO(ContentRegisterRequest contentRegisterRequest);

  @Named("mapDraftOptions")
  default List<ContentOptionDTO> mapDraftOptions(ContentDraftRequest request) {
    List<ContentOptionDTO> options = new ArrayList<>();

    // Coaching Options 변환
    if (request.getCoachingOptions() != null) {
      request
          .getCoachingOptions()
          .forEach(
              coachingOption -> {
                options.add(
                    ContentOptionDTO.builder()
                        .contentOptionId(null) // Draft 단계에서는 ID가 없음
                        .name(coachingOption.getName())
                        .description(coachingOption.getDescription())
                        .price(coachingOption.getPrice())
                        .contentType(liaison.groble.domain.content.enums.ContentType.COACHING)
                        .build());
              });
    }

    // Document Options 변환
    if (request.getDocumentOptions() != null) {
      request
          .getDocumentOptions()
          .forEach(
              documentOption -> {
                options.add(
                    ContentOptionDTO.builder()
                        .contentOptionId(null)
                        .name(documentOption.getName())
                        .description(documentOption.getDescription())
                        .price(documentOption.getPrice())
                        .contentType(liaison.groble.domain.content.enums.ContentType.DOCUMENT)
                        .documentOriginalFileName(null)
                        .documentFileUrl(documentOption.getDocumentFileUrl())
                        .documentLinkUrl(documentOption.getDocumentLinkUrl())
                        .build());
              });
    }

    return options;
  }

  @Named("mapRegisterOptions")
  default List<ContentOptionDTO> mapRegisterOptions(ContentRegisterRequest request) {
    List<ContentOptionDTO> options = new ArrayList<>();

    // Coaching Options 변환
    if (request.getCoachingOptions() != null) {
      request
          .getCoachingOptions()
          .forEach(
              coachingOption -> {
                options.add(
                    ContentOptionDTO.builder()
                        .contentOptionId(null) // Draft 단계에서는 ID가 없음
                        .name(coachingOption.getName())
                        .description(coachingOption.getDescription())
                        .price(coachingOption.getPrice())
                        .contentType(liaison.groble.domain.content.enums.ContentType.COACHING)
                        .build());
              });
    }

    // Document Options 변환
    if (request.getDocumentOptions() != null) {
      request
          .getDocumentOptions()
          .forEach(
              documentOption -> {
                options.add(
                    ContentOptionDTO.builder()
                        .contentOptionId(null)
                        .name(documentOption.getName())
                        .description(documentOption.getDescription())
                        .price(documentOption.getPrice())
                        .contentType(liaison.groble.domain.content.enums.ContentType.DOCUMENT)
                        .documentOriginalFileName(null)
                        .documentFileUrl(documentOption.getDocumentFileUrl())
                        .documentLinkUrl(documentOption.getDocumentLinkUrl())
                        .build());
              });
    }

    return options;
  }

  /** ContentCardDTO를 ContentPreviewCardResponse로 변환 */
  ContentPreviewCardResponse toContentPreviewCardResponse(ContentCardDTO contentCardDTO);

  default PageResponse<ContentPreviewCardResponse> toContentPreviewCardResponsePage(
      PageResponse<ContentCardDTO> DTOPage) {
    return toPageResponse(DTOPage, this::toContentPreviewCardResponse);
  }

  // ====== 📤 DTO → Response 변환 ======
  @Mapping(target = "id", source = "contentId")
  @Mapping(target = "options", source = "options", qualifiedByName = "mapOptionsToResponse")
  @Mapping(target = "subscriptionSellStatus", source = "subscriptionSellStatus")
  @Mapping(target = "contentDetailImageUrls", ignore = true)
  ContentResponse toContentResponse(ContentDTO contentDTO);

  @Mapping(target = "subscriptionSellStatus", source = "subscriptionSellStatus")
  ContentStatusResponse toContentStatusResponse(ContentDTO contentDTO);

  @Named("mapOptionsToResponse")
  default List<ContentResponse.OptionResponse> mapOptionsToResponse(
      List<ContentOptionDTO> options) {
    if (options == null) {
      return null;
    }

    return options.stream()
        .map(
            option ->
                ContentResponse.OptionResponse.builder()
                    .id(option.getContentOptionId())
                    .name(option.getName())
                    .description(option.getDescription())
                    .price(option.getPrice())
                    .hasSalesHistory(option.getHasSalesHistory())
                    .documentFileUrl(option.getDocumentFileUrl())
                    .documentLinkUrl(option.getDocumentLinkUrl())
                    .build())
        .collect(Collectors.toList());
  }

  // ====== 📤 ContentDetailDTO → ContentDetailResponse 변환 ======
  @Mapping(
      target = "priceOptionLength",
      expression =
          "java(contentDetailDTO.getOptions() != null ? contentDetailDTO.getOptions().size() : 0)")
  @Mapping(
      target = "options",
      source = "contentDetailDTO.options",
      qualifiedByName = "mapOptionsToBaseOptionResponse")
  @Mapping(target = "contactInfo", source = "contactInfoResponse")
  ContentDetailResponse toContentDetailResponse(
      ContentDetailDTO contentDetailDTO, ContactInfoResponse contactInfoResponse);

  @Named("mapOptionsToBaseOptionResponse")
  default List<BaseOptionResponse> mapOptionsToBaseOptionResponse(List<ContentOptionDTO> options) {
    if (options == null) {
      return null;
    }

    return options.stream()
        .map(
            option -> {
              if (option.getContentType()
                  == liaison.groble.domain.content.enums.ContentType.COACHING) {
                return CoachingOptionResponse.builder()
                    .optionId(option.getContentOptionId())
                    .name(option.getName())
                    .description(option.getDescription())
                    .price(option.getPrice())
                    .hasSalesHistory(option.getHasSalesHistory())
                    .build();
              } else {
                return DocumentOptionResponse.builder()
                    .optionId(option.getContentOptionId())
                    .name(option.getName())
                    .description(option.getDescription())
                    .price(option.getPrice())
                    .hasSalesHistory(option.getHasSalesHistory())
                    .documentOriginalFileName(option.getDocumentOriginalFileName())
                    .documentFileUrl(option.getDocumentFileUrl())
                    .documentLinkUrl(option.getDocumentLinkUrl())
                    .build();
              }
            })
        .collect(Collectors.toList());
  }
}
