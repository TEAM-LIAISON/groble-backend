package liaison.groble.mapping.dashboard;

import javax.annotation.processing.Generated;
import liaison.groble.api.model.dashboard.response.ContentOverviewResponse;
import liaison.groble.api.model.dashboard.response.ContentTotalViewStatsResponse;
import liaison.groble.api.model.dashboard.response.ContentViewStatsResponse;
import liaison.groble.api.model.dashboard.response.DashboardOverviewResponse;
import liaison.groble.api.model.dashboard.response.DashboardViewStatsResponse;
import liaison.groble.api.model.dashboard.response.MarketViewStatsResponse;
import liaison.groble.application.dashboard.dto.ContentTotalViewStatsDTO;
import liaison.groble.application.dashboard.dto.ContentViewStatsDTO;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardViewStatsDTO;
import liaison.groble.application.dashboard.dto.MarketViewStatsDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-21T12:05:29+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)"
)
@Component
public class DashboardMapperImpl implements DashboardMapper {

    @Override
    public DashboardOverviewResponse toDashboardOverviewResponse(DashboardOverviewDTO dashboardOverviewDTO) {
        if ( dashboardOverviewDTO == null ) {
            return null;
        }

        DashboardOverviewResponse.DashboardOverviewResponseBuilder dashboardOverviewResponse = DashboardOverviewResponse.builder();

        if ( dashboardOverviewDTO.getVerificationStatus() != null ) {
            dashboardOverviewResponse.verificationStatus( dashboardOverviewDTO.getVerificationStatus() );
        }
        if ( dashboardOverviewDTO.getTotalRevenue() != null ) {
            dashboardOverviewResponse.totalRevenue( dashboardOverviewDTO.getTotalRevenue() );
        }
        if ( dashboardOverviewDTO.getTotalSalesCount() != null ) {
            dashboardOverviewResponse.totalSalesCount( dashboardOverviewDTO.getTotalSalesCount() );
        }
        if ( dashboardOverviewDTO.getCurrentMonthRevenue() != null ) {
            dashboardOverviewResponse.currentMonthRevenue( dashboardOverviewDTO.getCurrentMonthRevenue() );
        }
        if ( dashboardOverviewDTO.getCurrentMonthSalesCount() != null ) {
            dashboardOverviewResponse.currentMonthSalesCount( dashboardOverviewDTO.getCurrentMonthSalesCount() );
        }
        if ( dashboardOverviewDTO.getTotalMarketViews() != null ) {
            dashboardOverviewResponse.totalMarketViews( dashboardOverviewDTO.getTotalMarketViews() );
        }
        if ( dashboardOverviewDTO.getTotalContentViews() != null ) {
            dashboardOverviewResponse.totalContentViews( dashboardOverviewDTO.getTotalContentViews() );
        }
        if ( dashboardOverviewDTO.getTotalCustomers() != null ) {
            dashboardOverviewResponse.totalCustomers( dashboardOverviewDTO.getTotalCustomers() );
        }
        if ( dashboardOverviewDTO.getRecentCustomers() != null ) {
            dashboardOverviewResponse.recentCustomers( dashboardOverviewDTO.getRecentCustomers() );
        }

        return dashboardOverviewResponse.build();
    }

    @Override
    public ContentTotalViewStatsResponse toContentTotalViewStatsResponse(ContentTotalViewStatsDTO contentTotalViewStatsDTO) {
        if ( contentTotalViewStatsDTO == null ) {
            return null;
        }

        ContentTotalViewStatsResponse.ContentTotalViewStatsResponseBuilder contentTotalViewStatsResponse = ContentTotalViewStatsResponse.builder();

        if ( contentTotalViewStatsDTO.getContentId() != null ) {
            contentTotalViewStatsResponse.contentId( contentTotalViewStatsDTO.getContentId() );
        }
        if ( contentTotalViewStatsDTO.getContentTitle() != null ) {
            contentTotalViewStatsResponse.contentTitle( contentTotalViewStatsDTO.getContentTitle() );
        }
        if ( contentTotalViewStatsDTO.getTotalViews() != null ) {
            contentTotalViewStatsResponse.totalViews( contentTotalViewStatsDTO.getTotalViews() );
        }

        return contentTotalViewStatsResponse.build();
    }

    @Override
    public ContentOverviewResponse toContentOverviewResponse(DashboardContentOverviewDTO dashboardContentOverviewDTO) {
        if ( dashboardContentOverviewDTO == null ) {
            return null;
        }

        ContentOverviewResponse.ContentOverviewResponseBuilder contentOverviewResponse = ContentOverviewResponse.builder();

        if ( dashboardContentOverviewDTO.getContentId() != null ) {
            contentOverviewResponse.contentId( dashboardContentOverviewDTO.getContentId() );
        }
        if ( dashboardContentOverviewDTO.getContentTitle() != null ) {
            contentOverviewResponse.contentTitle( dashboardContentOverviewDTO.getContentTitle() );
        }

        return contentOverviewResponse.build();
    }

    @Override
    public ContentViewStatsResponse toContentViewStatsResponse(ContentViewStatsDTO contentViewStatsDTO) {
        if ( contentViewStatsDTO == null ) {
            return null;
        }

        ContentViewStatsResponse.ContentViewStatsResponseBuilder contentViewStatsResponse = ContentViewStatsResponse.builder();

        if ( contentViewStatsDTO.getViewDate() != null ) {
            contentViewStatsResponse.viewDate( contentViewStatsDTO.getViewDate() );
        }
        if ( contentViewStatsDTO.getDayOfWeek() != null ) {
            contentViewStatsResponse.dayOfWeek( contentViewStatsDTO.getDayOfWeek() );
        }
        if ( contentViewStatsDTO.getViewCount() != null ) {
            contentViewStatsResponse.viewCount( contentViewStatsDTO.getViewCount() );
        }

        return contentViewStatsResponse.build();
    }

    @Override
    public MarketViewStatsResponse toMarketViewStatsResponse(MarketViewStatsDTO marketViewStatsDTO) {
        if ( marketViewStatsDTO == null ) {
            return null;
        }

        MarketViewStatsResponse.MarketViewStatsResponseBuilder marketViewStatsResponse = MarketViewStatsResponse.builder();

        if ( marketViewStatsDTO.getViewDate() != null ) {
            marketViewStatsResponse.viewDate( marketViewStatsDTO.getViewDate() );
        }
        if ( marketViewStatsDTO.getDayOfWeek() != null ) {
            marketViewStatsResponse.dayOfWeek( marketViewStatsDTO.getDayOfWeek() );
        }
        if ( marketViewStatsDTO.getViewCount() != null ) {
            marketViewStatsResponse.viewCount( marketViewStatsDTO.getViewCount() );
        }

        return marketViewStatsResponse.build();
    }

    @Override
    public DashboardViewStatsResponse toDashboardViewStatsResponse(DashboardViewStatsDTO dashboardViewStatsDTO) {
        if ( dashboardViewStatsDTO == null ) {
            return null;
        }

        DashboardViewStatsResponse.DashboardViewStatsResponseBuilder dashboardViewStatsResponse = DashboardViewStatsResponse.builder();

        if ( dashboardViewStatsDTO.getTotalMarketViews() != null ) {
            dashboardViewStatsResponse.totalMarketViews( dashboardViewStatsDTO.getTotalMarketViews() );
        }
        if ( dashboardViewStatsDTO.getTotalContentViews() != null ) {
            dashboardViewStatsResponse.totalContentViews( dashboardViewStatsDTO.getTotalContentViews() );
        }

        return dashboardViewStatsResponse.build();
    }
}
