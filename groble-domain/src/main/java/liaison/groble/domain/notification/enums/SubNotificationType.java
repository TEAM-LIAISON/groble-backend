package liaison.groble.domain.notification.enums;

public enum SubNotificationType {
  // REVIEW [심사 관련]
  CONTENT_REVIEW_APPROVED, // 심사 승인
  CONTENT_REVIEW_REJECTED, // 심사 거절

  // CERTIFY [인증 관련]
  MAKER_CERTIFIED, // 메이커 인증 완료
  MAKER_CERTIFY_REJECTED, // 메이커 인증 거절

  // SYSTEM [시스템 관련]
  WELCOME_GROBLE
}
