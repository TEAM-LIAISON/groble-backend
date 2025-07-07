package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.response.AdminOrderCancelRequestResponse;
import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminMapper {
  AdminOrderCancelRequestResponse toAdminOrderCancelRequestResponse(
      AdminOrderCancelRequestDTO adminOrderCancelRequestDTO);
}
