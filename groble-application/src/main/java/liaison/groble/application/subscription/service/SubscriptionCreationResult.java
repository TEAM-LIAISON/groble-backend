package liaison.groble.application.subscription.service;

import liaison.groble.domain.subscription.entity.Subscription;

/**
 * 정기결제 생성/갱신 결과.
 *
 * @param subscription 생성 또는 갱신된 구독 엔티티
 * @param renewed true이면 기존 구독 갱신, false이면 신규 생성
 */
public record SubscriptionCreationResult(Subscription subscription, boolean renewed) {

  public static SubscriptionCreationResult created(Subscription subscription) {
    return new SubscriptionCreationResult(subscription, false);
  }

  public static SubscriptionCreationResult renewed(Subscription subscription) {
    return new SubscriptionCreationResult(subscription, true);
  }

  public boolean isNewSubscription() {
    return !renewed;
  }
}
