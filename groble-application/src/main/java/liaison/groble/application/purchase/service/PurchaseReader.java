package liaison.groble.application.purchase.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.purchase.repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseReader {
  private final PurchaseRepository purchaseRepository;
}
