package liaison.groble.mapping.dashboard;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.dashboard.response.ContentOverviewResponse;
import liaison.groble.api.model.dashboard.response.DashboardOverviewResponse;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-16T18:45:49+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class DashboardMapperImpl implements DashboardMapper {

  @Override
  public DashboardOverviewResponse toDashboardOverviewResponse(
      DashboardOverviewDTO dashboardOverviewDTO) {
    if (dashboardOverviewDTO == null) {
      return null;
    }

    DashboardOverviewResponse.DashboardOverviewResponseBuilder dashboardOverviewResponse =
        DashboardOverviewResponse.builder();

    if (dashboardOverviewDTO.getVerificationStatus() != null) {
      dashboardOverviewResponse.verificationStatus(dashboardOverviewDTO.getVerificationStatus());
    }
    if (dashboardOverviewDTO.getTotalRevenue() != null) {
      dashboardOverviewResponse.totalRevenue(dashboardOverviewDTO.getTotalRevenue());
    }
    if (dashboardOverviewDTO.getTotalSalesCount() != null) {
      dashboardOverviewResponse.totalSalesCount(dashboardOverviewDTO.getTotalSalesCount());
    }
    if (dashboardOverviewDTO.getCurrentMonthRevenue() != null) {
      dashboardOverviewResponse.currentMonthRevenue(dashboardOverviewDTO.getCurrentMonthRevenue());
    }
    if (dashboardOverviewDTO.getCurrentMonthSalesCount() != null) {
      dashboardOverviewResponse.currentMonthSalesCount(
          dashboardOverviewDTO.getCurrentMonthSalesCount());
    }
    if (dashboardOverviewDTO.getTotalMarketViews() != null) {
      dashboardOverviewResponse.totalMarketViews(dashboardOverviewDTO.getTotalMarketViews());
    }
    if (dashboardOverviewDTO.getTotalContentViews() != null) {
      dashboardOverviewResponse.totalContentViews(dashboardOverviewDTO.getTotalContentViews());
    }
    if (dashboardOverviewDTO.getTotalCustomers() != null) {
      dashboardOverviewResponse.totalCustomers(dashboardOverviewDTO.getTotalCustomers());
    }
    if (dashboardOverviewDTO.getRecentCustomers() != null) {
      dashboardOverviewResponse.recentCustomers(dashboardOverviewDTO.getRecentCustomers());
    }

    return dashboardOverviewResponse.build();
  }

  @Override
  public ContentOverviewResponse toContentOverviewResponse(
      DashboardContentOverviewDTO dashboardContentOverviewDTO) {
    if (dashboardContentOverviewDTO == null) {
      return null;
    }

    ContentOverviewResponse.ContentOverviewResponseBuilder contentOverviewResponse =
        ContentOverviewResponse.builder();

    if (dashboardContentOverviewDTO.getTotalContentsCount() != null) {
      contentOverviewResponse.totalContentsCount(
          dashboardContentOverviewDTO.getTotalContentsCount());
    }
    if (dashboardContentOverviewDTO.getContentId() != null) {
      contentOverviewResponse.contentId(dashboardContentOverviewDTO.getContentId());
    }
    if (dashboardContentOverviewDTO.getContentTitle() != null) {
      contentOverviewResponse.contentTitle(dashboardContentOverviewDTO.getContentTitle());
    }

    return contentOverviewResponse.build();
  }
}
