package liaison.groble.mapping.dashboard;

import org.mapstruct.Mapper;

import liaison.groble.api.model.dashboard.response.DashboardOverviewResponse;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface DashboardMapper extends PageResponseMapper {
  DashboardOverviewResponse toDashboardOverviewResponse(DashboardOverviewDTO dashboardOverviewDTO);
}
