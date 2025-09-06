package liaison.groble.application.admin.dashboard.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dashboard.dto.AdminDashboardOverviewDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.guest.repository.GuestUserRepository;
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

  @Transactional(readOnly = true)
  public AdminDashboardOverviewDTO getAdminDashboardOverview() {
    // 거래 통계 조회
    FlatDashboardOverviewDTO transactionStats = purchaseReader.getAdminDashboardOverviewStats();

    // 사용자 통계 조회
    long userCount = userRepository.countByStatus(UserStatus.ACTIVE);
    long guestUserCount = guestUserRepository.count();

    // 콘텐츠 통계 조회
    List<ContentStatus> allStatuses =
        Arrays.asList(ContentStatus.DRAFT, ContentStatus.ACTIVE, ContentStatus.DISCONTINUED);
    List<ContentStatus> activeStatuses = Arrays.asList(ContentStatus.DRAFT, ContentStatus.ACTIVE);

    long totalContentCount = contentRepository.countByStatusIn(allStatuses);
    long activeContentCount = contentRepository.countByStatusIn(activeStatuses);
    long documentTypeCount =
        contentRepository.countByContentTypeAndStatusIn("DOCUMENT", allStatuses);
    long coachingTypeCount =
        contentRepository.countByContentTypeAndStatusIn("COACHING", allStatuses);
    long membershipTypeCount = 0L;
    //        contentRepository.countByContentTypeAndStatusIn("MEMBERSHIP", activeStatuses);

    return AdminDashboardOverviewDTO.builder()
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
}
