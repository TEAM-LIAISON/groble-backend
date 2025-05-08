package liaison.groble.api.server.scrap.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.response.ContentScrapCardResponse;
import liaison.groble.api.model.scrap.response.UpdateContentScrapStateResponse;
import liaison.groble.application.scrap.dto.ContentScrapCardDto;
import liaison.groble.application.scrap.dto.ContentScrapDto;

@Component
public class ContentScrapDtoMapper {

  public UpdateContentScrapStateResponse toUpdateContentScrapStateResponse(
      ContentScrapDto contentScrapDto) {
    return UpdateContentScrapStateResponse.builder()
        .contentId(contentScrapDto.getContentId())
        .isContentScrap(contentScrapDto.getIsContentScrap())
        .build();
  }

  public ContentScrapCardResponse toContentScrapCardFromCardDto(ContentScrapCardDto cardDto) {
    return ContentScrapCardResponse.builder()
        .contentId(cardDto.getContentId())
        .contentType(cardDto.getContentType())
        .title(cardDto.getTitle())
        .thumbnailUrl(cardDto.getThumbnailUrl())
        .sellerName(cardDto.getSellerName())
        .isContentScrap(cardDto.getIsContentScrap())
        .build();
  }
}
