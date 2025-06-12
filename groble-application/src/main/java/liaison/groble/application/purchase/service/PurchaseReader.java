package liaison.groble.application.purchase.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseReader {
  private final PurchaseRepository purchaseRepository;

  public Purchase getPurchaseByOrderId(Long orderId) {
    return purchaseRepository
        .findByOrderId(orderId)
        .orElseThrow(() -> new EntityNotFoundException("구매 정보를 찾을 수 없습니다. Order ID: " + orderId));
  }
}
