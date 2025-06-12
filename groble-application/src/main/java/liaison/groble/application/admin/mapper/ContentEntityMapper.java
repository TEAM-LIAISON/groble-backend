package liaison.groble.application.admin.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import liaison.groble.application.content.dto.ContentDto;
import liaison.groble.application.content.dto.ContentOptionDto;
import liaison.groble.domain.content.entity.CoachingOption;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.entity.DocumentOption;
import liaison.groble.domain.content.enums.ContentStatus;

@Component
public class ContentEntityMapper {

  /** Content Entity를 ContentDto로 변환합니다. */
  public ContentDto toDto(Content content) {
    if (content == null) {
      return null;
    }

    return ContentDto.builder()
        .contentId(content.getId())
        .title(content.getTitle())
        .thumbnailUrl(content.getThumbnailUrl())
        .contentType(getContentTypeName(content))
        .status(getStatusName(content))
        .categoryId(getCategoryId(content))
        .contentIntroduction(content.getContentIntroduction())
        .serviceTarget(content.getServiceTarget())
        .serviceProcess(content.getServiceProcess())
        .makerIntro(content.getMakerIntro())
        .options(convertOptions(content.getOptions()))
        .build();
  }

  private String getContentTypeName(Content content) {
    return content.getContentType() != null ? content.getContentType().name() : null;
  }

  private String getStatusName(Content content) {
    return content.getStatus() != null ? content.getStatus().name() : ContentStatus.DRAFT.name();
  }

  private String getCategoryId(Content content) {
    return content.getCategory() != null ? content.getCategory().getCode() : null;
  }

  private List<ContentOptionDto> convertOptions(List<ContentOption> options) {
    if (options == null || options.isEmpty()) {
      return null;
    }

    List<ContentOptionDto> optionDtos = new ArrayList<>();
    for (ContentOption option : options) {
      if (option != null) {
        optionDtos.add(convertSingleOption(option));
      }
    }

    return optionDtos.isEmpty() ? null : optionDtos;
  }

  private ContentOptionDto convertSingleOption(ContentOption option) {
    ContentOptionDto.ContentOptionDtoBuilder builder =
        ContentOptionDto.builder()
            .contentOptionId(option.getId())
            .name(option.getName())
            .description(option.getDescription())
            .price(option.getPrice());

    if (option instanceof CoachingOption coachingOption) {
      return addCoachingOptionFields(builder, coachingOption);
    } else if (option instanceof DocumentOption documentOption) {
      return addDocumentOptionFields(builder, documentOption);
    }

    return builder.build();
  }

  private ContentOptionDto addCoachingOptionFields(
      ContentOptionDto.ContentOptionDtoBuilder builder, CoachingOption coachingOption) {

    return builder
        .coachingPeriod(getEnumName(coachingOption.getCoachingPeriod()))
        .documentProvision(getEnumName(coachingOption.getDocumentProvision()))
        .coachingType(getEnumName(coachingOption.getCoachingType()))
        .coachingTypeDescription(coachingOption.getCoachingTypeDescription())
        .build();
  }

  private ContentOptionDto addDocumentOptionFields(
      ContentOptionDto.ContentOptionDtoBuilder builder, DocumentOption documentOption) {

    return builder
        .contentDeliveryMethod(getEnumName(documentOption.getContentDeliveryMethod()))
        .documentFileUrl(documentOption.getDocumentFileUrl())
        .documentLinkUrl(documentOption.getDocumentLinkUrl())
        .build();
  }

  private String getEnumName(Enum<?> enumValue) {
    return enumValue != null ? enumValue.name() : null;
  }
}
