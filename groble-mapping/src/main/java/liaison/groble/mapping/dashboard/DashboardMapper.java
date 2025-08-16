package liaison.groble.mapping.dashboard;

import org.mapstruct.Mapper;

import liaison.groble.api.model.dashboard.response.ContentOverviewResponse;
import liaison.groble.api.model.dashboard.response.DashboardOverviewResponse;
import liaison.groble.api.model.dashboard.response.MarketViewStatsResponse;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.application.dashboard.dto.MarketViewStatsDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface DashboardMapper extends PageResponseMapper {
  DashboardOverviewResponse toDashboardOverviewResponse(DashboardOverviewDTO dashboardOverviewDTO);

  default PageResponse<ContentOverviewResponse> toContentOverviewResponsePage(
      PageResponse<DashboardContentOverviewDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toContentOverviewResponse);
  }

  default PageResponse<MarketViewStatsResponse> toMarketViewStatsResponsePage(
      PageResponse<MarketViewStatsDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toMarketViewStatsResponse);
  }

  ContentOverviewResponse toContentOverviewResponse(
      DashboardContentOverviewDTO dashboardContentOverviewDTO);

  MarketViewStatsResponse toMarketViewStatsResponse(MarketViewStatsDTO marketViewStatsDTO);
}
