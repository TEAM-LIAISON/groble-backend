package liaison.groble.application.admin.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dashboard.dto.AdminActiveGuestSessionDTO;
import liaison.groble.application.admin.dashboard.dto.AdminActiveMemberSessionDTO;
import liaison.groble.application.admin.dashboard.dto.AdminActiveVisitorsDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardOverviewDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTopContentDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTopContentsDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTrendDTO;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardTrendPointDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.session.ActiveSessionService;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.guest.repository.GuestUserRepository;
import liaison.groble.domain.purchase.dto.FlatDailyTransactionStatDTO;
import liaison.groble.domain.purchase.dto.FlatTopContentStatDTO;
import liaison.groble.domain.session.GuestActiveSession;
import liaison.groble.domain.session.MemberActiveSession;
import liaison.groble.domain.user.entity.User;
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
  private final ActiveSessionService activeSessionService;

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

  @Transactional(readOnly = true)
  public AdminActiveVisitorsDTO getActiveVisitors(Duration window, int limit) {
    Duration effectiveWindow =
        (window == null || window.isZero() || window.isNegative()) ? Duration.ofMinutes(5) : window;
    if (effectiveWindow.compareTo(Duration.ofHours(6)) > 0) {
      effectiveWindow = Duration.ofHours(6);
    }

    int effectiveLimit = limit <= 0 ? 50 : Math.min(limit, 200);

    var snapshot = activeSessionService.getActiveSessions(effectiveWindow, effectiveLimit);

    List<MemberActiveSession> memberSessions = snapshot.getMemberSessions();
    List<GuestActiveSession> guestSessions = snapshot.getGuestSessions();

    Map<Long, User> memberMap = fetchActiveUsers(memberSessions);
    Map<Long, GuestUser> guestMap = fetchActiveGuestUsers(guestSessions);

    List<AdminActiveMemberSessionDTO> memberDtos =
        memberSessions.stream()
            .map(session -> toAdminMemberSessionDTO(session, memberMap.get(session.getUserId())))
            .collect(Collectors.toList());

    List<AdminActiveGuestSessionDTO> guestDtos =
        guestSessions.stream()
            .map(session -> toAdminGuestSessionDTO(session, guestMap.get(session.getGuestId())))
            .collect(Collectors.toList());

    return AdminActiveVisitorsDTO.builder()
        .windowMinutes((int) effectiveWindow.toMinutes())
        .limit(effectiveLimit)
        .generatedAt(toLocalDateTime(snapshot.getGeneratedAt()))
        .memberSessions(memberDtos)
        .guestSessions(guestDtos)
        .memberCount(memberDtos.size())
        .guestCount(guestDtos.size())
        .build();
  }

  private Map<Long, User> fetchActiveUsers(List<MemberActiveSession> sessions) {
    Map<Long, User> users = new java.util.HashMap<>();
    if (sessions == null || sessions.isEmpty()) {
      return users;
    }

    Set<Long> userIds =
        sessions.stream()
            .map(MemberActiveSession::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    for (Long userId : userIds) {
      userRepository.findById(userId).ifPresent(user -> users.put(userId, user));
    }

    return users;
  }

  private Map<Long, GuestUser> fetchActiveGuestUsers(List<GuestActiveSession> sessions) {
    Map<Long, GuestUser> guests = new java.util.HashMap<>();
    if (sessions == null || sessions.isEmpty()) {
      return guests;
    }

    Set<Long> guestIds =
        sessions.stream()
            .map(GuestActiveSession::getGuestId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    for (Long guestId : guestIds) {
      guestUserRepository.findById(guestId).ifPresent(guest -> guests.put(guestId, guest));
    }

    return guests;
  }

  private AdminActiveMemberSessionDTO toAdminMemberSessionDTO(
      MemberActiveSession session, User user) {
    String nickname = null;
    String phoneNumber = null;
    if (user != null && user.getUserProfile() != null) {
      nickname = user.getUserProfile().getNickname();
      phoneNumber = user.getUserProfile().getPhoneNumber();
    }

    String email = user != null ? user.getEmail() : null;

    return AdminActiveMemberSessionDTO.builder()
        .sessionKey(session.getSessionKey())
        .userId(session.getUserId())
        .nickname(nickname)
        .email(email)
        .phoneNumber(phoneNumber)
        .accountType(session.getAccountType())
        .lastUserType(session.getLastUserType())
        .roles(session.getRoles())
        .requestUri(session.getRequestUri())
        .httpMethod(session.getHttpMethod())
        .queryString(session.getQueryString())
        .referer(session.getReferer())
        .clientIp(session.getClientIp())
        .userAgent(session.getUserAgent())
        .lastSeenAt(toLocalDateTime(session.getLastSeenAt()))
        .build();
  }

  private AdminActiveGuestSessionDTO toAdminGuestSessionDTO(
      GuestActiveSession session, GuestUser guest) {
    String displayName = null;
    String email = null;
    String phoneNumber = null;
    if (guest != null) {
      displayName = guest.getUsername();
      email = guest.getEmail();
      phoneNumber = guest.getPhoneNumber();
    }

    return AdminActiveGuestSessionDTO.builder()
        .sessionKey(session.getSessionKey())
        .guestId(session.getGuestId())
        .authenticated(session.isAuthenticated())
        .displayName(displayName)
        .email(email)
        .phoneNumber(phoneNumber)
        .anonymousId(session.getAnonymousId())
        .requestUri(session.getRequestUri())
        .httpMethod(session.getHttpMethod())
        .queryString(session.getQueryString())
        .referer(session.getReferer())
        .clientIp(session.getClientIp())
        .userAgent(session.getUserAgent())
        .lastSeenAt(toLocalDateTime(session.getLastSeenAt()))
        .build();
  }

  private LocalDateTime toLocalDateTime(Instant instant) {
    if (instant == null) {
      return null;
    }
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
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
