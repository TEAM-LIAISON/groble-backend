package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.response.AdminMakerDetailInfoResponse;
import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminMakerMapper {
  AdminMakerDetailInfoResponse toAdminMakerDetailInfoResponse(
      AdminMakerDetailInfoDTO adminMakerDetailInfoDTO);
}
