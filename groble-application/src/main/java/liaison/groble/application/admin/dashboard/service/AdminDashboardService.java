package liaison.groble.application.admin.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dashboard.dto.AdminDashboardOverviewDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTopContentDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTopContentsDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTrendDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTrendPointDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.guest.repository.GuestUserRepository;
import liaison.groble.domain.purchase.dto.FlatDailyTransactionStatDTO;
import liaison.groble.domain.purchase.dto.FlatTopContentStatDTO;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 대시보드 서비스
 *
 * <p>관리자가 대시보드를 조회하는 비즈니스 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminDashboardService {

  // Repository
  private final UserRepository userRepository;
  private final ContentRepository contentRepository;
  private final GuestUserRepository guestUserRepository;

  // Reader
  private final PurchaseReader purchaseReader;
  private final SettlementReader settlementReader;

  private static final int DEFAULT_TOP_CONTENT_LIMIT = 5;

  @Transactional(readOnly = true)
  public AdminDashboardOverviewDTO getAdminDashboardOverview() {
    // 거래 통계 조회
    FlatDashboardOverviewDTO transactionStats = purchaseReader.getAdminDashboardOverviewStats();

    // 정산 통계 조회 (완료된 정산의 플랫폼 수수료 합계)
    BigDecimal grobleFee = settlementReader.getTotalCompletedPlatformFee();

    // 사용자 통계 조회
    long userCount = userRepository.countByStatus(UserStatus.ACTIVE);
    long guestUserCount = guestUserRepository.count();

    // 콘텐츠 통계 조회
    List<ContentStatus> allStatuses =
        Arrays.asList(ContentStatus.DRAFT, ContentStatus.ACTIVE, ContentStatus.DISCONTINUED);
    List<ContentStatus> activeStatuses = Arrays.asList(ContentStatus.ACTIVE);

    long totalContentCount = contentRepository.countByStatusIn(allStatuses);
    long activeContentCount = contentRepository.countByStatusIn(activeStatuses);
    long documentTypeCount =
        contentRepository.countByContentTypeAndStatusIn("DOCUMENT", allStatuses);
    long coachingTypeCount =
        contentRepository.countByContentTypeAndStatusIn("COACHING", allStatuses);
    long membershipTypeCount = 0L;
    //        contentRepository.countByContentTypeAndStatusIn("MEMBERSHIP", activeStatuses);

    return AdminDashboardOverviewDTO.builder()
        .grobleFee(grobleFee)
        .etcAmount(BigDecimal.ZERO) // 추가된 필드, 현재는 0으로 설정
        .totalTransactionAmount(transactionStats.getTotalRevenue())
        .totalTransactionCount(transactionStats.getTotalSalesCount())
        .monthlyTransactionAmount(transactionStats.getCurrentMonthRevenue())
        .monthlyTransactionCount(transactionStats.getCurrentMonthSalesCount())
        .userCount(userCount)
        .guestUserCount(guestUserCount)
        .totalContentCount(totalContentCount)
        .activeContentCount(activeContentCount)
        .documentTypeCount(documentTypeCount)
        .coachingTypeCount(coachingTypeCount)
        .membershipTypeCount(membershipTypeCount)
        .build();
  }

  @Transactional(readOnly = true)
  public AdminDashboardTrendDTO getAdminDashboardTrends(LocalDate startDate, LocalDate endDate) {
    validateDateRange(startDate, endDate);

    List<FlatDailyTransactionStatDTO> rawStats =
        purchaseReader.getAdminDailyTransactionStats(startDate, endDate);

    BigDecimal totalRevenue =
        rawStats.stream()
            .map(FlatDailyTransactionStatDTO::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    long totalSalesCount =
        rawStats.stream()
            .map(FlatDailyTransactionStatDTO::getTotalSalesCount)
            .filter(count -> count != null)
            .mapToLong(Long::longValue)
            .sum();

    BigDecimal overallAverageOrderValue = calculateAverage(totalRevenue, totalSalesCount);

    List<AdminDashboardTrendPointDTO> dailyStats =
        rawStats.stream()
            .map(
                stat ->
                    AdminDashboardTrendPointDTO.builder()
                        .date(stat.getDate())
                        .totalRevenue(stat.getTotalRevenue())
                        .totalSalesCount(stat.getTotalSalesCount())
                        .averageOrderValue(
                            calculateAverage(stat.getTotalRevenue(), stat.getTotalSalesCount()))
                        .build())
            .collect(Collectors.toList());

    return AdminDashboardTrendDTO.builder()
        .startDate(startDate)
        .endDate(endDate)
        .totalRevenue(totalRevenue)
        .totalSalesCount(totalSalesCount)
        .averageOrderValue(overallAverageOrderValue)
        .daily(dailyStats)
        .build();
  }

  @Transactional(readOnly = true)
  public AdminDashboardTopContentsDTO getAdminTopContents(
      LocalDate startDate, LocalDate endDate, Integer limit) {
    validateDateRange(startDate, endDate);

    int effectiveLimit =
        (limit == null || limit <= 0) ? DEFAULT_TOP_CONTENT_LIMIT : Math.min(limit, 50);

    List<FlatTopContentStatDTO> topContentStats =
        purchaseReader.getAdminTopContentStats(startDate, endDate, effectiveLimit);

    List<AdminDashboardTopContentDTO> contents =
        topContentStats.stream()
            .map(
                stat ->
                    AdminDashboardTopContentDTO.builder()
                        .contentId(stat.getContentId())
                        .contentTitle(stat.getContentTitle())
                        .sellerId(stat.getSellerId())
                        .sellerNickname(stat.getSellerNickname())
                        .totalRevenue(stat.getTotalRevenue())
                        .totalSalesCount(stat.getTotalSalesCount())
                        .averageOrderValue(
                            calculateAverage(stat.getTotalRevenue(), stat.getTotalSalesCount()))
                        .build())
            .collect(Collectors.toList());

    return AdminDashboardTopContentsDTO.builder()
        .startDate(startDate)
        .endDate(endDate)
        .limit(effectiveLimit)
        .contents(contents)
        .build();
  }

  private void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("날짜 범위가 필요합니다.");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("시작일은 종료일보다 앞서야 합니다.");
    }
  }

  private BigDecimal calculateAverage(BigDecimal revenue, long count) {
    if (revenue == null || count <= 0) {
      return BigDecimal.ZERO;
    }
    return revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
  }
}
