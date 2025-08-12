package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.request.AdminMemoRequest;
import liaison.groble.api.model.admin.response.maker.AdminMakerDetailInfoResponse;
import liaison.groble.api.model.admin.response.maker.AdminMemoResponse;
import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;
import liaison.groble.application.admin.dto.AdminMemoDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminMakerMapper {
  AdminMakerDetailInfoResponse toAdminMakerDetailInfoResponse(
      AdminMakerDetailInfoDTO adminMakerDetailInfoDTO);

  AdminMemoDTO toAdminMemoDTO(AdminMemoRequest adminMemoRequest);

  AdminMemoResponse toAdminMemoResponse(AdminMemoDTO adminMemoDTO);
}
