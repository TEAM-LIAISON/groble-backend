package liaison.groble.mapping.dashboard;

import org.mapstruct.Mapper;

import liaison.groble.api.model.dashboard.response.ContentOverviewResponse;
import liaison.groble.api.model.dashboard.response.ContentViewStatsResponse;
import liaison.groble.api.model.dashboard.response.DashboardOverviewResponse;
import liaison.groble.api.model.dashboard.response.DashboardViewStatsResponse;
import liaison.groble.api.model.dashboard.response.MarketViewStatsResponse;
import liaison.groble.application.dashboard.dto.ContentViewStatsDTO;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardViewStatsDTO;
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

  default PageResponse<ContentViewStatsResponse> toContentViewStatsResponsePage(
      PageResponse<ContentViewStatsDTO> dtoPage) {
    return toPageResponse(dtoPage, this::toContentViewStatsResponse);
  }

  ContentOverviewResponse toContentOverviewResponse(
      DashboardContentOverviewDTO dashboardContentOverviewDTO);

  ContentViewStatsResponse toContentViewStatsResponse(ContentViewStatsDTO contentViewStatsDTO);

  MarketViewStatsResponse toMarketViewStatsResponse(MarketViewStatsDTO marketViewStatsDTO);

  DashboardViewStatsResponse toDashboardViewStatsResponse(
      DashboardViewStatsDTO dashboardViewStatsDTO);
}
