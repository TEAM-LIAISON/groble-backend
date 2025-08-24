package liaison.groble.application.dashboard.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import liaison.groble.domain.dashboard.repository.MarketReferrerStatsCustomRepository;
import liaison.groble.domain.dashboard.repository.MarketViewStatsCustomRepository;
import liaison.groble.domain.dashboard.repository.MarketViewStatsRepository;
import liaison.groble.domain.market.entity.Market;
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

  // Repository
  private final ContentViewStatsRepository contentViewStatsRepository;
  private final ContentViewStatsCustomRepository contentViewStatsCustomRepository;
  private final ContentReferrerStatsCustomRepository contentReferrerStatsCustomRepository;
  private final MarketReferrerStatsCustomRepository marketReferrerStatsCustomRepository;
  private final MarketViewStatsRepository marketViewStatsRepository;
  private final MarketViewStatsCustomRepository marketViewStatsCustomRepository;

  @Transactional(readOnly = true)
  public DashboardOverviewDTO getDashboardOverview(Long userId) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    FlatDashboardOverviewDTO flatDashboardOverviewDTO =
        purchaseReader.getDashboardOverviewStats(userId);

    Long totalContentViews =
        getTotalContentViews(userId, LocalDate.of(2025, 8, 1), LocalDate.now());
    Long totalMarketViews = getTotalMarketViews(userId, LocalDate.of(2025, 8, 1), LocalDate.now());

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
  public DashboardViewStatsDTO getViewStats(Long userId, String period) {
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
    Long totalContentViews = getTotalContentViews(userId, startDate, endDate);
    Long totalMarketViews = getTotalMarketViews(userId, startDate, endDate);

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
      Long userId, String period, Pageable pageable) {
    Market market = userReader.getMarket(userId);
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
            market.getMarketLinkUrl(), PeriodType.DAILY, startDate, endDate, pageable);
    // ========== 여기서부터 추가/수정 ==========
    // 1. 전체 날짜 리스트 생성
    List<LocalDate> allDates =
        startDate
            .datesUntil(endDate.plusDays(1))
            .sorted(Comparator.reverseOrder()) // 내림차순 정렬
            .collect(Collectors.toList());

    // 2. DB에서 조회한 데이터를 Map으로 변환
    Map<LocalDate, FlatMarketViewStatsDTO> dataMap =
        page.getContent().stream()
            .collect(Collectors.toMap(FlatMarketViewStatsDTO::getViewDate, Function.identity()));

    // 3. 페이징 처리
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allDates.size());
    List<LocalDate> pagedDates = allDates.subList(start, end);

    // 4. 모든 날짜에 대한 데이터 생성 (없는 날짜는 0으로)
    List<FlatMarketViewStatsDTO> completeData =
        pagedDates.stream()
            .map(
                date ->
                    dataMap.getOrDefault(
                        date,
                        FlatMarketViewStatsDTO.builder()
                            .viewDate(date)
                            .dayOfWeek("") // toMarketViewStatsDTO에서 계산하므로 빈 문자열
                            .viewCount(0L)
                            .build()))
            .collect(Collectors.toList());

    // 5. 새로운 Page 객체 생성
    Page<FlatMarketViewStatsDTO> completePage =
        new PageImpl<>(completeData, pageable, allDates.size());
    // ========== 여기까지 추가/수정 ==========

    // 총 조회수 계산 (completePage 사용)
    long totalViews =
        completePage.getContent().stream()
            .mapToLong(dto -> dto.getViewCount() != null ? dto.getViewCount() : 0L)
            .sum();

    List<MarketViewStatsDTO> items =
        completePage.getContent().stream()
            .map(this::toMarketViewStatsDTO)
            .collect(Collectors.toList());

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .totalViews(totalViews)
            .build();

    return PageResponse.from(completePage, items, meta);
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

  @Transactional(readOnly = true)
  public PageResponse<ReferrerStatsDTO> getMarketReferrerStats(
      Long userId, String period, Pageable pageable) {

    Market market = userReader.getMarket(userId);

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
        marketReferrerStatsCustomRepository.findMarketReferrerStats(
            market.getMarketLinkUrl(), startDate, endDate, pageable);

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

  // 총 컨텐츠 조회수
  private Long getTotalContentViews(Long sellerId, LocalDate startDate, LocalDate endDate) {
    List<Long> contentIds = contentReader.findIdsByUserId(sellerId);
    return contentViewStatsRepository.getTotalContentViews(contentIds, startDate, endDate);
  }

  // 총 마켓 조회수
  private Long getTotalMarketViews(Long sellerId, LocalDate startDate, LocalDate endDate) {
    return marketViewStatsRepository.getTotalMarketViews(sellerId, startDate, endDate);
  }

  private DashboardContentOverviewDTO toDashboardContentOverviewDTO(
      FlatContentOverviewDTO flatContentOverviewDTO) {
    return DashboardContentOverviewDTO.builder()
        .contentId(flatContentOverviewDTO.getContentId())
        .contentTitle(flatContentOverviewDTO.getContentTitle())
        .build();
  }
}
