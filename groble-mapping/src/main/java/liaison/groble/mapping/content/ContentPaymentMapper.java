package liaison.groble.mapping.content;

import org.mapstruct.Mapper;

import liaison.groble.api.model.content.response.pay.ContentPayPageResponse;
import liaison.groble.application.content.dto.ContentPayPageDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ContentPaymentMapper {
  ContentPayPageResponse toContentPayPageResponse(ContentPayPageDTO contentPayPageDTO);
}
