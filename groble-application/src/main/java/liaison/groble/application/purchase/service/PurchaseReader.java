package liaison.groble.application.purchase.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.dto.FlatSellManageDetailDTO;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseCustomRepository;
import liaison.groble.domain.purchase.repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseReader {
  private final PurchaseRepository purchaseRepository;
  private final PurchaseCustomRepository purchaseCustomRepository;

  // 주문 번호로 내가 구매한 구매 정보를 Order Fetch Join 단일 조회
  public Purchase getPurchaseWithOrderAndContent(String merchantUid, Long userId) {
    return purchaseRepository
        .findByMerchantUidAndUserIdWithOrderAndContent(merchantUid, userId)
        .orElseThrow(() -> new EntityNotFoundException("구매 정보를 찾을 수 없습니다."));
  }

  public FlatPurchaseContentDetailDTO getPurchaseContentDetail(Long userId, String merchantUid) {
    return purchaseCustomRepository
        .getPurchaseContentDetail(userId, merchantUid)
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

  public boolean isContentPurchasedByUser(Long userId, Long contentId) {
    return purchaseCustomRepository.existsByUserAndContent(userId, contentId);
  }
}
