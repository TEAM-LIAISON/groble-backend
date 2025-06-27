package liaison.groble.mapping.content;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import liaison.groble.api.model.content.request.draft.ContentDraftRequest;
import liaison.groble.api.model.content.request.register.ContentRegisterRequest;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.ContentResponse;
import liaison.groble.api.model.content.response.ContentStatusResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.dto.ContentOptionDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ContentMapper {
  // ====== 📥 Request → DTO 변환 ======
  @Mapping(target = "options", source = ".", qualifiedByName = "mapDraftOptions")
  @Mapping(target = "status", ignore = true)
  ContentDTO toContentDTO(ContentDraftRequest contentDraftRequest);

  @Mapping(target = "options", source = ".", qualifiedByName = "mapRegisterOptions")
  @Mapping(target = "status", ignore = true)
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
                        .coachingPeriod(coachingOption.getCoachingPeriod())
                        .documentProvision(coachingOption.getDocumentProvision())
                        .coachingType(coachingOption.getCoachingType())
                        .coachingTypeDescription(coachingOption.getCoachingTypeDescription())
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
                        .contentDeliveryMethod(documentOption.getContentDeliveryMethod())
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
                        .coachingPeriod(coachingOption.getCoachingPeriod())
                        .documentProvision(coachingOption.getDocumentProvision())
                        .coachingType(coachingOption.getCoachingType())
                        .coachingTypeDescription(coachingOption.getCoachingTypeDescription())
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
                        .contentDeliveryMethod(documentOption.getContentDeliveryMethod())
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
      PageResponse<ContentCardDTO> dtoPageResponse) {
    if (dtoPageResponse == null) {
      return null;
    }

    // items 리스트 변환
    List<ContentPreviewCardResponse> convertedItems =
        dtoPageResponse.getItems().stream()
            .map(this::toContentPreviewCardResponse)
            .collect(Collectors.toList());

    // PageInfo 복사
    PageResponse.PageInfo pageInfo =
        PageResponse.PageInfo.builder()
            .currentPage(dtoPageResponse.getPageInfo().getCurrentPage())
            .totalPages(dtoPageResponse.getPageInfo().getTotalPages())
            .pageSize(dtoPageResponse.getPageInfo().getPageSize())
            .totalElements(dtoPageResponse.getPageInfo().getTotalElements())
            .first(dtoPageResponse.getPageInfo().isFirst())
            .last(dtoPageResponse.getPageInfo().isLast())
            .empty(dtoPageResponse.getPageInfo().isEmpty())
            .build();

    // MetaData 복사 (있는 경우)
    PageResponse.MetaData meta = null;
    if (dtoPageResponse.getMeta() != null) {
      meta =
          PageResponse.MetaData.builder()
              .searchTerm(dtoPageResponse.getMeta().getSearchTerm())
              .filter(dtoPageResponse.getMeta().getFilter())
              .sortBy(dtoPageResponse.getMeta().getSortBy())
              .sortDirection(dtoPageResponse.getMeta().getSortDirection())
              .categoryIds(dtoPageResponse.getMeta().getCategoryIds())
              .build();
    }

    // PageResponse 생성
    return PageResponse.<ContentPreviewCardResponse>builder()
        .items(convertedItems)
        .pageInfo(pageInfo)
        .meta(meta)
        .build();
  }

  // ====== 📤 DTO → Response 변환 ======
  @Mapping(target = "id", source = "contentId")
  @Mapping(target = "options", source = "options", qualifiedByName = "mapOptionsToResponse")
  @Mapping(target = "contentDetailImageUrls", ignore = true)
  ContentResponse toContentResponse(ContentDTO contentDTO);

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
                    // coaching 전용 필드들
                    .coachingPeriod(option.getCoachingPeriod())
                    .documentProvision(option.getDocumentProvision())
                    .coachingType(option.getCoachingType())
                    .coachingTypeDescription(option.getCoachingTypeDescription())
                    // document 전용 필드들
                    .contentDeliveryMethod(option.getContentDeliveryMethod())
                    .documentFileUrl(option.getDocumentFileUrl())
                    .documentLinkUrl(option.getDocumentLinkUrl())
                    .build())
        .collect(Collectors.toList());
  }
}
