package liaison.groble.mapping.content;

import org.mapstruct.Mapper;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ContentMapper {
  // ====== 📥 Request → DTO 변환 ======
  ContentPreviewCardResponse toContentPreviewCardResponse(ContentCardDTO contentCardDto);
}
