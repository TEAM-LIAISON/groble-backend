package liaison.groble.application.purchase.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatDailyTransactionStatDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.dto.FlatSellManageDetailDTO;
import liaison.groble.domain.purchase.dto.FlatTopContentStatDTO;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseCustomRepository;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseReader {
  private final PurchaseRepository purchaseRepository;
  private final PurchaseCustomRepository purchaseCustomRepository;
  private final SubscriptionRepository subscriptionRepository;

  // 주문 번호로 내가 구매한 구매 정보를 Order Fetch Join 단일 조회
  public Purchase getPurchaseWithOrderAndContent(String merchantUid, Long userId) {
    return purchaseRepository
        .findByMerchantUidAndUserIdWithOrderAndContent(merchantUid, userId)
        .orElseThrow(() -> new EntityNotFoundException("구매 정보를 찾을 수 없습니다."));
  }

  public Purchase getGuestPurchaseWithOrderAndContent(String merchantUid, Long guestUserId) {
    return purchaseRepository
        .findByMerchantUidAndGuestUserIdWithOrderAndContent(merchantUid, guestUserId)
        .orElseThrow(() -> new EntityNotFoundException("구매 정보를 찾을 수 없습니다."));
  }

  public FlatPurchaseContentDetailDTO getPurchaseContentDetail(Long userId, String merchantUid) {
    return purchaseCustomRepository
        .getPurchaseContentDetail(userId, merchantUid)
        .orElseThrow(
            () -> new EntityNotFoundException("구매 정보를 찾을 수 없습니다. Merchant UID: " + merchantUid));
  }

  // 비회원 구매 상세 정보 조회
  public FlatPurchaseContentDetailDTO getPurchaseContentDetailForGuest(String merchantUid) {
    return purchaseCustomRepository
        .getPurchaseContentDetailForGuest(merchantUid)
        .orElseThrow(
            () -> new EntityNotFoundException("구매 정보를 찾을 수 없습니다. Merchant UID: " + merchantUid));
  }

  // 주문 ID로 구매 정보 조회
  public Purchase getPurchaseByOrderId(Long orderId) {
    return purchaseRepository
        .findByOrderId(orderId)
        .orElseThrow(() -> new EntityNotFoundException("구매 정보를 찾을 수 없습니다. Order ID: " + orderId));
  }

  public FlatContentSellDetailDTO getContentSellDetail(
      Long userId, Long contentId, Long purchaseId) {
    return purchaseCustomRepository
        .getContentSellDetailDTO(userId, contentId, purchaseId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "판매 상세 정보를 찾을 수 없습니다. User ID: "
                        + userId
                        + ", Content ID: "
                        + contentId
                        + ", Purchase ID: "
                        + purchaseId));
  }

  public Page<FlatContentSellDetailDTO> getContentSells(
      Long userId, Long contentId, Pageable pageable) {
    return purchaseCustomRepository.getContentSellPageDTOs(userId, contentId, pageable);
  }

  public Page<FlatPurchaseContentPreviewDTO> findMyPurchasedContents(
      Long userId, List<Order.OrderStatus> orderStatuses, Pageable pageable) {
    return purchaseCustomRepository.findMyPurchasedContents(userId, orderStatuses, pageable);
  }

  public Page<FlatPurchaseContentPreviewDTO> findMyPurchasedContentsForGuest(
      String guestPhoneNumber, List<Order.OrderStatus> orderStatuses, Pageable pageable) {
    return purchaseCustomRepository.findMyPurchasedContentsForGuest(
        guestPhoneNumber, orderStatuses, pageable);
  }

  public FlatSellManageDetailDTO getSellManageDetail(Long userId, Long contentId) {
    return purchaseCustomRepository
        .getSellManageDetail(userId, contentId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "판매 관리 정보를 찾을 수 없습니다. User ID: " + userId + ", Content ID: " + contentId));
  }

  // 대시보드 개요 조회
  public FlatDashboardOverviewDTO getDashboardOverviewStats(Long sellerId) {
    return purchaseCustomRepository.getDashboardOverviewStats(sellerId);
  }

  // 관리자 대시보드 개요 조회
  public FlatDashboardOverviewDTO getAdminDashboardOverviewStats() {
    return purchaseCustomRepository.getAdminDashboardOverviewStats();
  }

  public List<FlatDailyTransactionStatDTO> getAdminDailyTransactionStats(
      LocalDate startDate, LocalDate endDate) {
    return purchaseCustomRepository.getAdminDailyTransactionStats(startDate, endDate);
  }

  public List<FlatTopContentStatDTO> getAdminTopContentStats(
      LocalDate startDate, LocalDate endDate, long limit) {
    return purchaseCustomRepository.getAdminTopContentStats(startDate, endDate, limit);
  }

  public boolean isContentPurchasedByUser(Long userId, Long contentId) {
    return purchaseCustomRepository.existsByUserAndContent(userId, contentId);
  }

  public boolean isContentPurchasedByGuestUser(Long guestUserId, Long contentId) {
    return purchaseCustomRepository.existsByGuestUserAndContent(guestUserId, contentId);
  }

  /**
   * 유저가 콘텐츠에 접근 가능한지 확인 (구독 및 유예기간 포함)
   *
   * <p>다음 경우에 접근 가능:
   *
   * <ul>
   *   <li>구매 기록이 있고 취소되지 않은 경우
   *   <li>구독이 활성화(ACTIVE, PAST_DUE) 상태인 경우
   *   <li>구독이 유예기간 중(CANCELLED + grace_period_ends_at > now)인 경우
   * </ul>
   *
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @return 접근 가능하면 true
   */
  public boolean hasAccessToContent(Long userId, Long contentId) {
    // 1. 구독 확인 (ACTIVE, PAST_DUE, 유예기간 중 CANCELLED)
    return subscriptionRepository
        .findByContentIdAndUserId(contentId, userId)
        .map(
            subscription -> {
              SubscriptionStatus status = subscription.getStatus();

              // ACTIVE 또는 PAST_DUE 상태면 접근 가능
              if (EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE)
                  .contains(status)) {
                return true;
              }

              // CANCELLED이지만 유예기간 중이면 접근 가능
              if (status == SubscriptionStatus.CANCELLED
                  && subscription.isGracePeriodActive(LocalDateTime.now())) {
                return true;
              }

              return false;
            })
        .orElseGet(
            () -> {
              // 2. 구독이 없으면 일반 구매 확인
              return purchaseCustomRepository.existsByUserAndContent(userId, contentId);
            });
  }

  /**
   * 비회원이 콘텐츠에 접근 가능한지 확인 (비회원은 구독이 없으므로 구매 기록만 확인)
   *
   * @param guestUserId 비회원 사용자 ID
   * @param contentId 콘텐츠 ID
   * @return 접근 가능하면 true
   */
  public boolean hasAccessToContentForGuest(Long guestUserId, Long contentId) {
    return purchaseCustomRepository.existsByGuestUserAndContent(guestUserId, contentId);
  }
}
