package liaison.groble.application.dashboard.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentOverviewDTO;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.dashboard.repository.ContentViewStatsRepository;
import liaison.groble.domain.dashboard.repository.MarketViewStatsRepository;
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
  private final MarketViewStatsRepository marketViewStatsRepository;

  @Transactional(readOnly = true)
  public DashboardOverviewDTO getDashboardOverview(Long userId) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    FlatDashboardOverviewDTO flatDashboardOverviewDTO =
        purchaseReader.getDashboardOverviewStats(userId);

    Long totalContentViews = getTotalContentViews(userId);
    Long totalMarketViews = getTotalMarketViews(userId);

    return DashboardOverviewDTO.builder()
        .verificationStatus(sellerInfo.getVerificationStatus().name())
        .totalRevenue(flatDashboardOverviewDTO.getTotalRevenue())
        .totalSalesCount(flatDashboardOverviewDTO.getTotalSalesCount())
        .currentMonthRevenue(flatDashboardOverviewDTO.getCurrentMonthRevenue())
        .currentMonthSalesCount(flatDashboardOverviewDTO.getCurrentMonthSalesCount())
        .totalMarketViews(totalMarketViews)
        .totalContentViews(totalContentViews)
        .totalCustomers(flatDashboardOverviewDTO.getTotalCustomers())
        .recentCustomers(flatDashboardOverviewDTO.getRecentCustomers())
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<DashboardContentOverviewDTO> getMyContentsList(
      Long userId, Pageable pageable) {
    Page<FlatContentOverviewDTO> page = contentReader.findMyContentsBySellerId(userId, pageable);

    List<DashboardContentOverviewDTO> items =
        page.getContent().stream().map(this::toDashboardContentOverviewDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  private Long getTotalContentViews(Long sellerId) {
    List<Long> contentIds = contentReader.findIdsByUserId(sellerId);
    return contentViewStatsRepository.getTotalContentViews(
        contentIds, LocalDate.of(2025, 8, 1), LocalDate.now());
  }

  private Long getTotalMarketViews(Long sellerId) {
    return marketViewStatsRepository.getTotalMarketViews(
        sellerId, LocalDate.of(2025, 8, 1), LocalDate.now());
  }

  private DashboardContentOverviewDTO toDashboardContentOverviewDTO(
      FlatContentOverviewDTO flatContentOverviewDTO) {
    return DashboardContentOverviewDTO.builder()
        .totalContentsCount(flatContentOverviewDTO.getTotalContentsCount())
        .contentId(flatContentOverviewDTO.getContentId())
        .contentTitle(flatContentOverviewDTO.getContentTitle())
        .build();
  }
}
