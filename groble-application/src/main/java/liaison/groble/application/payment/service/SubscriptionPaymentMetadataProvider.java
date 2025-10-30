package liaison.groble.application.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;

import liaison.groble.application.payment.dto.billing.BillingKeyAction;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentMetadata;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.payment.entity.BillingKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionPaymentMetadataProvider {

  private static final ZoneId BILLING_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final String DEFAULT_PAY_METHOD = "CARD";
  private static final String DEFAULT_CARD_VER = "02";
  private static final String DEFAULT_REGULAR_FLAG = "Y";

  private final BillingKeyService billingKeyService;

  public Optional<SubscriptionPaymentMetadata> buildForContent(
      Long userId, Content content, ContentOption option) {
    if (content == null || content.getPaymentType() != ContentPaymentType.SUBSCRIPTION) {
      return Optional.empty();
    }

    BigDecimal price = option != null ? option.getPrice() : BigDecimal.ZERO;
    LocalDate nextPaymentDate = calculateNextPaymentDate();

    return Optional.of(buildMetadata(userId, price, nextPaymentDate));
  }

  public Optional<SubscriptionPaymentMetadata> buildForOrder(Order order) {
    if (order == null) {
      return Optional.empty();
    }

    OrderItem primaryItem = order.getOrderItems().isEmpty() ? null : order.getOrderItems().get(0);
    Content content = primaryItem != null ? primaryItem.getContent() : null;
    if (content == null || content.getPaymentType() != ContentPaymentType.SUBSCRIPTION) {
      return Optional.empty();
    }

    Long userId = order.getUser() != null ? order.getUser().getId() : null;
    BigDecimal finalPrice = order.getFinalPrice();
    LocalDate nextPaymentDate = calculateNextPaymentDate();

    return Optional.of(buildMetadata(userId, finalPrice, nextPaymentDate));
  }

  private SubscriptionPaymentMetadata buildMetadata(
      Long userId, BigDecimal amount, LocalDate nextPaymentDate) {
    boolean requiresImmediateCharge = requiresImmediateCharge(amount);

    Optional<BillingKey> activeBillingKey =
        userId != null ? billingKeyService.findActiveBillingKey(userId) : Optional.empty();
    boolean hasActiveBillingKey = activeBillingKey.isPresent();
    BillingKeyAction action = determineAction(hasActiveBillingKey, requiresImmediateCharge);

    return SubscriptionPaymentMetadata.builder()
        .billingKeyAction(action)
        .hasActiveBillingKey(hasActiveBillingKey)
        .billingKeyId(activeBillingKey.map(BillingKey::getBillingKey).orElse(null))
        .merchantUserKey(userId != null ? userId.toString() : null)
        .defaultPayMethod(DEFAULT_PAY_METHOD)
        .payWork(action.getPayWork())
        .cardVer(DEFAULT_CARD_VER)
        .regularFlag(DEFAULT_REGULAR_FLAG)
        .nextPaymentDate(nextPaymentDate)
        .payYear(formatYear(nextPaymentDate))
        .payMonth(formatMonth(nextPaymentDate))
        .payDay(formatDay(nextPaymentDate))
        .requiresImmediateCharge(requiresImmediateCharge)
        .build();
  }

  private LocalDate calculateNextPaymentDate() {
    return LocalDate.now(BILLING_ZONE_ID).plusMonths(1);
  }

  private boolean requiresImmediateCharge(BigDecimal amount) {
    if (amount == null) {
      return false;
    }
    return amount.compareTo(BigDecimal.ZERO) > 0;
  }

  private BillingKeyAction determineAction(boolean hasActiveBillingKey, boolean requiresCharge) {
    if (hasActiveBillingKey) {
      return BillingKeyAction.REUSE;
    }
    return requiresCharge ? BillingKeyAction.REGISTER_AND_CHARGE : BillingKeyAction.REGISTER;
  }

  private String formatYear(LocalDate date) {
    return date != null ? String.format("%04d", date.getYear()) : null;
  }

  private String formatMonth(LocalDate date) {
    return date != null ? String.format("%02d", date.getMonthValue()) : null;
  }

  private String formatDay(LocalDate date) {
    return date != null ? String.format("%02d", date.getDayOfMonth()) : null;
  }
}
