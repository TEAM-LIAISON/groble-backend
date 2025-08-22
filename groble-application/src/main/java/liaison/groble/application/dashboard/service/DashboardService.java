package liaison.groble.application.dashboard.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.dashboard.dto.ContentTotalViewStatsDTO;
import liaison.groble.application.dashboard.dto.ContentViewStatsDTO;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardViewStatsDTO;
import liaison.groble.application.dashboard.dto.MarketViewStatsDTO;
import liaison.groble.application.dashboard.dto.referrer.ReferrerStatsDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.content.dto.FlatContentOverviewDTO;
import liaison.groble.domain.dashboard.dto.FlatContentTotalViewStatsDTO;
import liaison.groble.domain.dashboard.dto.FlatContentViewStatsDTO;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.dashboard.dto.FlatMarketViewStatsDTO;
import liaison.groble.domain.dashboard.dto.FlatReferrerStatsDTO;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsCustomRepository;
import liaison.groble.domain.dashboard.repository.ContentViewStatsCustomRepository;
import liaison.groble.domain.dashboard.repository.ContentViewStatsRepository;
import liaison.groble.domain.dashboard.repository.MarketViewStatsCustomRepository;
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
  private final ContentViewStatsCustomRepository contentViewStatsCustomRepository;
  private final ContentReferrerStatsCustomRepository contentReferrerStatsCustomRepository;
  private final MarketViewStatsRepository marketViewStatsRepository;
  private final MarketViewStatsCustomRepository marketViewStatsCustomRepository;

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

  @Transactional(readOnly = true)
  public DashboardViewStatsDTO getViewStats(Long userId) {
    Long totalContentViews = getTotalContentViews(userId);
    Long totalMarketViews = getTotalMarketViews(userId);

    return DashboardViewStatsDTO.builder()
        .totalContentViews(totalContentViews)
        .totalMarketViews(totalMarketViews)
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentTotalViewStatsDTO> getContentTotalViewStats(
      Long userId, String period, Pageable pageable) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate =
        switch (period) {
          case "TODAY" -> endDate;
          case "LAST_7_DAYS" -> endDate.minusDays(6);
          case "LAST_30_DAYS" -> endDate.minusDays(29);
          case "THIS_MONTH" -> endDate.withDayOfMonth(1);
          case "LAST_MONTH" -> {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            yield lastMonth.atDay(1);
          }
          default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

    Page<FlatContentTotalViewStatsDTO> page =
        contentViewStatsCustomRepository.findTotalViewsByPeriodTypeAndStatDateBetween(
            PeriodType.DAILY, startDate, endDate, pageable);

    List<ContentTotalViewStatsDTO> items =
        page.getContent().stream()
            .map(this::toContentTotalViewStatsDTO)
            .collect(Collectors.toList());

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public PageResponse<MarketViewStatsDTO> getMarketViewStats(
      Long userId, Long marketId, String period, Pageable pageable) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate =
        switch (period) {
          case "TODAY" -> endDate;
          case "LAST_7_DAYS" -> endDate.minusDays(6);
          case "LAST_30_DAYS" -> endDate.minusDays(29);
          case "THIS_MONTH" -> endDate.withDayOfMonth(1);
          case "LAST_MONTH" -> {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            yield lastMonth.atDay(1);
          }
          default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

    Page<FlatMarketViewStatsDTO> page =
        marketViewStatsCustomRepository.findByMarketIdAndPeriodTypeAndStatDateBetween(
            marketId, PeriodType.DAILY, startDate, endDate, pageable);

    // 총 조회수 계산
    long totalViews =
        page.getContent().stream()
            .mapToLong(dto -> dto.getViewCount() != null ? dto.getViewCount() : 0L)
            .sum();

    List<MarketViewStatsDTO> items =
        page.getContent().stream().map(this::toMarketViewStatsDTO).collect(Collectors.toList());

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .totalViews(totalViews)
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentViewStatsDTO> getContentViewStats(
      Long userId, Long contentId, String period, Pageable pageable) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate =
        switch (period) {
          case "TODAY" -> endDate;
          case "LAST_7_DAYS" -> endDate.minusDays(6);
          case "LAST_30_DAYS" -> endDate.minusDays(29);
          case "THIS_MONTH" -> endDate.withDayOfMonth(1);
          case "LAST_MONTH" -> {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            yield lastMonth.atDay(1);
          }
          default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

    Page<FlatContentViewStatsDTO> page =
        contentViewStatsCustomRepository.findByContentIdAndPeriodTypeAndStatDateBetween(
            contentId, PeriodType.DAILY, startDate, endDate, pageable);

    // 총 조회수 계산
    long totalViews =
        page.getContent().stream()
            .mapToLong(dto -> dto.getViewCount() != null ? dto.getViewCount() : 0L)
            .sum();

    List<ContentViewStatsDTO> items =
        page.getContent().stream().map(this::toContentViewStatsDTO).collect(Collectors.toList());

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .totalViews(totalViews)
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public PageResponse<ReferrerStatsDTO> getContentReferrerStats(
      Long userId, Long contentId, String period, Pageable pageable) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate =
        switch (period) {
          case "TODAY" -> endDate;
          case "LAST_7_DAYS" -> endDate.minusDays(6);
          case "LAST_30_DAYS" -> endDate.minusDays(29);
          case "THIS_MONTH" -> endDate.withDayOfMonth(1);
          case "LAST_MONTH" -> {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            yield lastMonth.atDay(1);
          }
          default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

    Page<FlatReferrerStatsDTO> page =
        contentReferrerStatsCustomRepository.findContentReferrerStats(
            contentId, startDate, endDate, pageable);

    List<ReferrerStatsDTO> items =
        page.getContent().stream().map(this::toReferrerStatsDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  private ReferrerStatsDTO toReferrerStatsDTO(FlatReferrerStatsDTO flatReferrerStatsDTO) {
    return ReferrerStatsDTO.builder()
        .referrerUrl(flatReferrerStatsDTO.getReferrerUrl())
        .visitCount(flatReferrerStatsDTO.getVisitCount())
        .build();
  }

  private MarketViewStatsDTO toMarketViewStatsDTO(FlatMarketViewStatsDTO flatMarketViewStatsDTO) {
    return MarketViewStatsDTO.builder()
        .viewDate(flatMarketViewStatsDTO.getViewDate())
        .dayOfWeek(
            flatMarketViewStatsDTO
                .getViewDate()
                .getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN))
        .viewCount(flatMarketViewStatsDTO.getViewCount())
        .build();
  }

  private ContentTotalViewStatsDTO toContentTotalViewStatsDTO(
      FlatContentTotalViewStatsDTO flatContentTotalViewStatsDTO) {
    return ContentTotalViewStatsDTO.builder()
        .contentId(flatContentTotalViewStatsDTO.getContentId())
        .contentTitle(flatContentTotalViewStatsDTO.getContentTitle())
        .totalViews(flatContentTotalViewStatsDTO.getTotalViews())
        .build();
  }

  private ContentViewStatsDTO toContentViewStatsDTO(
      FlatContentViewStatsDTO flatContentViewStatsDTO) {
    return ContentViewStatsDTO.builder()
        .viewDate(flatContentViewStatsDTO.getViewDate())
        .dayOfWeek(
            flatContentViewStatsDTO
                .getViewDate()
                .getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN))
        .viewCount(flatContentViewStatsDTO.getViewCount())
        .build();
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
        .contentId(flatContentOverviewDTO.getContentId())
        .contentTitle(flatContentOverviewDTO.getContentTitle())
        .build();
  }
}
