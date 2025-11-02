package liaison.groble.mapping.content;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.content.response.review.ContentDetailReviewResponse;
import liaison.groble.api.model.content.response.review.ContentReviewResponse;
import liaison.groble.application.content.dto.review.ContentDetailReviewDTO;
import liaison.groble.application.content.dto.review.ContentReviewDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ContentReviewMapper {
  ContentReviewResponse toContentReviewResponse(ContentReviewDTO contentReviewDTO);

  @Mapping(
      target = "reviewerNickname",
      expression = "java(maskNickname(contentDetailReviewDTO.getReviewerNickname()))")
  ContentDetailReviewResponse toContentDetailReviewResponse(
      ContentDetailReviewDTO contentDetailReviewDTO);

  String REVIEWER_NICKNAME_MASK_SUFFIX = "*****";

  default String maskNickname(String nickname) {
    if (nickname == null || nickname.isBlank()) {
      return REVIEWER_NICKNAME_MASK_SUFFIX;
    }

    int firstCodePoint = nickname.codePointAt(0);
    String firstCharacter = new String(Character.toChars(firstCodePoint));
    return firstCharacter + REVIEWER_NICKNAME_MASK_SUFFIX;
  }
}
