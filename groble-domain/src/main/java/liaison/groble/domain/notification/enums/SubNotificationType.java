package liaison.groble.domain.notification.enums;

public enum SubNotificationType {
  // SELLER [판매자 관련]
  SELLER_VERIFIED,
  SELLER_REJECTED,

  // CONTENT [콘텐츠 관련]
  CONTENT_APPROVED,
  CONTENT_REJECTED,

  // INQUIRY [문의하기 관련]
  INQUIRY_START, // 문의 시작

  // SYSTEM [시스템 관련]
  WELCOME_GROBLE
}
