package liaison.groble.mapping.admin;

import java.util.List;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.dashboard.response.AdminActiveGuestSessionResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminActiveMemberSessionResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminActiveVisitorsResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardOverviewResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardTopContentResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardTopContentsResponse;
import liaison.groble.api.model.admin.dashboard.response.AdminDashboardTrendResponse;
import liaison.groble.application.admin.dashboard.dto.AdminActiveGuestSessionDTO;
import liaison.groble.application.admin.dashboard.dto.AdminActiveMemberSessionDTO;
import liaison.groble.application.admin.dashboard.dto.AdminActiveVisitorsDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardOverviewDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTopContentDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTopContentsDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTrendDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTrendPointDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminDashboardMapper {

  AdminDashboardOverviewResponse toAdminDashboardOverviewResponse(
      AdminDashboardOverviewDTO adminDashboardOverviewDTO);

  AdminDashboardTrendResponse toAdminDashboardTrendResponse(
      AdminDashboardTrendDTO adminDashboardTrendDTO);

  AdminDashboardTrendResponse.DailyMetricResponse toDailyMetricResponse(
      AdminDashboardTrendPointDTO trendPointDTO);

  AdminDashboardTopContentsResponse toAdminDashboardTopContentsResponse(
      AdminDashboardTopContentsDTO topContentsDTO);

  AdminDashboardTopContentResponse toAdminDashboardTopContentResponse(
      AdminDashboardTopContentDTO topContentDTO);

  List<AdminDashboardTopContentResponse> toAdminDashboardTopContentResponses(
      List<AdminDashboardTopContentDTO> topContentDTOs);

  AdminActiveVisitorsResponse toAdminActiveVisitorsResponse(
      AdminActiveVisitorsDTO activeVisitorsDTO);

  AdminActiveMemberSessionResponse toAdminActiveMemberSessionResponse(
      AdminActiveMemberSessionDTO memberSessionDTO);

  List<AdminActiveMemberSessionResponse> toAdminActiveMemberSessionResponses(
      List<AdminActiveMemberSessionDTO> memberSessionDTOs);

  AdminActiveGuestSessionResponse toAdminActiveGuestSessionResponse(
      AdminActiveGuestSessionDTO guestSessionDTO);

  List<AdminActiveGuestSessionResponse> toAdminActiveGuestSessionResponses(
      List<AdminActiveGuestSessionDTO> guestSessionDTOs);
}
