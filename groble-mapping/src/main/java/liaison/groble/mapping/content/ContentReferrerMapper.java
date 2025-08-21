package liaison.groble.mapping.content;

import org.mapstruct.Mapper;

import liaison.groble.api.model.content.request.referrer.ContentReferrerRequest;
import liaison.groble.application.content.dto.referrer.ContentReferrerDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ContentReferrerMapper {
  ContentReferrerDTO toContentReferrerDTO(ContentReferrerRequest contentReferrerRequest);
}
