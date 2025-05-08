package liaison.groble.api.server.scrap.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.scrap.response.UpdateContentScrapStateResponse;
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
}
