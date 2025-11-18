package liaison.groble.application.subscription.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.entity.Purchaser;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionRecurringOrderFactory {

  private final OrderRepository orderRepository;

  public Order createOrder(Subscription subscription) {
    if (subscription.getUser() == null) {
      throw new IllegalStateException("구독 정보에 회원 정보가 없습니다. subscriptionId=" + subscription.getId());
    }
    if (subscription.getContent() == null) {
      throw new IllegalStateException(
          "구독 정보에 콘텐츠 정보가 없습니다. subscriptionId=" + subscription.getId());
    }
    Long optionId = subscription.getOptionId();
    if (optionId == null) {
      throw new IllegalStateException("구독 정보에 옵션 ID가 없습니다. subscriptionId=" + subscription.getId());
    }

    BigDecimal price = subscription.getPrice();
    if (price == null) {
      throw new IllegalStateException("구독 금액 정보가 없습니다. subscriptionId=" + subscription.getId());
    }

    OrderItem.OptionType optionType = resolveOptionType(subscription);
    Purchaser purchaser = resolvePurchaser(subscription);

    Order order =
        Order.createOrderWithOption(
            subscription.getUser(),
            subscription.getContent(),
            optionType,
            optionId,
            price,
            purchaser);

    order = orderRepository.save(order);
    order.setMerchantUid(Order.generateMerchantUid(order.getId()));
    order = orderRepository.save(order);

    log.debug(
        "정기결제용 주문 생성 완료 - subscriptionId: {}, orderId: {}, merchantUid: {}",
        subscription.getId(),
        order.getId(),
        order.getMerchantUid());

    return order;
  }

  private OrderItem.OptionType resolveOptionType(Subscription subscription) {
    Purchase purchase = subscription.getPurchase();
    if (purchase != null && purchase.getSelectedOptionType() != null) {
      try {
        return OrderItem.OptionType.valueOf(purchase.getSelectedOptionType().name());
      } catch (IllegalArgumentException ex) {
        log.warn(
            "구독 옵션 타입 변환 실패 - subscriptionId: {}, optionType: {}",
            subscription.getId(),
            purchase.getSelectedOptionType(),
            ex);
      }
    }
    return OrderItem.OptionType.DOCUMENT_OPTION;
  }

  private Purchaser resolvePurchaser(Subscription subscription) {
    Purchase purchase = subscription.getPurchase();
    if (purchase != null
        && purchase.getOrder() != null
        && purchase.getOrder().getPurchaser() != null) {
      Purchaser previous = purchase.getOrder().getPurchaser();
      return Purchaser.builder()
          .name(previous.getName())
          .email(previous.getEmail())
          .phone(previous.getPhone())
          .build();
    }

    User user = subscription.getUser();
    String name = user.getNickname() != null ? user.getNickname() : "구독자";
    String email =
        user.getEmail() != null
            ? user.getEmail()
            : String.format("user-%d@groble.local", user.getId());
    String phone = user.getPhoneNumber();

    return Purchaser.builder().name(name).email(email).phone(phone).build();
  }
}
