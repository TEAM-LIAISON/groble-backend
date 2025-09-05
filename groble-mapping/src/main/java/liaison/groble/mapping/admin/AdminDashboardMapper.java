package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.dashboard.response.AdminDashboardOverviewResponse;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardOverviewDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminDashboardMapper {

  AdminDashboardOverviewResponse toAdminDashboardOverviewResponse(
      AdminDashboardOverviewDTO adminDashboardOverviewDTO);
}
