package liaison.groble.domain.content.enums;

/**
 * 판매자가 제공하는 정기결제 상품의 판매 정책 상태를 나타냅니다.
 *
 * <p>OPEN: 신규 구독 신청 가능
 *
 * <p>PAUSED: 기존 구독자는 결제가 유지되지만 신규 구독은 불가
 *
 * <p>TERMINATED: 신규 구독 불가 및 기존 구독도 더 이상 갱신되지 않음
 */
public enum SubscriptionSellStatus {
  OPEN,
  PAUSED,
  TERMINATED
}
