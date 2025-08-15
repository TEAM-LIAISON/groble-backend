package liaison.groble.application.dashboard.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.dashboard.repository.ContentViewStatsRepository;
import liaison.groble.domain.user.entity.SellerInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
  // Reader
  private final UserReader userReader;
  private final ContentReader contentReader;
  private final PurchaseReader purchaseReader;
  private final ContentViewStatsRepository contentViewStatsRepository;

  @Transactional(readOnly = true)
  public DashboardOverviewDTO getDashboardOverview(Long userId) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    FlatDashboardOverviewDTO flatDashboardOverviewDTO =
        purchaseReader.getDashboardOverviewStats(userId);

    Long totalContentViews = getTotalContentViews(userId);

    return DashboardOverviewDTO.builder()
        .verificationStatus(sellerInfo.getVerificationStatus().name())
        .totalRevenue(flatDashboardOverviewDTO.getTotalRevenue())
        .totalSalesCount(flatDashboardOverviewDTO.getTotalSalesCount())
        .currentMonthRevenue(flatDashboardOverviewDTO.getCurrentMonthRevenue())
        .currentMonthSalesCount(flatDashboardOverviewDTO.getCurrentMonthSalesCount())
        .totalMarketViews(0L)
        .totalContentViews(totalContentViews)
        .totalCustomers(flatDashboardOverviewDTO.getTotalCustomers())
        .recentCustomers(flatDashboardOverviewDTO.getRecentCustomers())
        .build();
  }

  private Long getTotalContentViews(Long sellerId) {
    List<Long> contentIds = contentReader.findIdsByUserId(sellerId);
    return contentViewStatsRepository.getTotalContentViews(
        contentIds, LocalDate.of(2025, 8, 1), LocalDate.now());
  }
}
