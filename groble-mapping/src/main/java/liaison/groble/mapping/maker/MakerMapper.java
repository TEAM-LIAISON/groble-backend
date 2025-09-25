package liaison.groble.mapping.maker;

import org.mapstruct.Mapper;

import liaison.groble.api.model.maker.response.MakerInfoResponse;
import liaison.groble.application.maker.dto.MakerInfoDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface MakerMapper {

  MakerInfoResponse toMakerInfoResponse(MakerInfoDTO dto);
}
