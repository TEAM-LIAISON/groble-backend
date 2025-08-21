package liaison.groble.mapping.dashboard;

import org.mapstruct.Mapper;

import liaison.groble.api.model.dashboard.request.referrer.ReferrerRequest;
import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ReferrerMapper {
  ReferrerDTO toContentReferrerDTO(ReferrerRequest referrerRequest);
}
