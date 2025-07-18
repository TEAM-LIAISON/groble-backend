package liaison.groble.mapping.admin;

import liaison.groble.api.model.admin.response.AdminMakerDetailInfoResponse;
import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;

public interface AdminMakerMapper {
  AdminMakerDetailInfoResponse toAdminMakerDetailInfoResponse(
      AdminMakerDetailInfoDTO adminMakerDetailInfoDTO);
}
