package liaison.groble.domain.notification.enums;

public enum SubNotificationType {
  // PURCHASE [구매 관련]
  CONTENT_REVIEW_REPLY, // 콘텐츠 리뷰 답글  [✅ 리뷰에 답글이 달렸어요]
  CONTENT_PURCHASED, // 콘텐츠 구매 [✅ 상품을 구매했어요]

  // REVIEW [리뷰 관련]
  CONTENT_REVIEWED, // 콘텐츠 리뷰 작성  [✅ 리뷰가 등록됐어요] - 📄[V]

  // SELL [판매 관련]
  CONTENT_SOLD, // 콘텐츠 판매 [✅ 상품이 판매됐어요] - 📄[V]
  CONTENT_SOLD_STOPPED, // 콘텐츠 판매 중단 [✅ 상품 판매가 중단됐어요] - 📄[V]

  // CERTIFY [인증 관련]
  MAKER_CERTIFIED, // 메이커 인증 완료 [✅ 메이커 인증이 완료됐어요] - 📄[V]
  MAKER_CERTIFY_REJECTED, // 메이커 인증 반려 [✅ 메이커 인증이 반려됐어요] - 📄[V]

  // SYSTEM [시스템 관련]
  WELCOME_GROBLE // 그로블 환영 [✅ 그로블에 오신 것을 환영합니다!] - 📄[V]
}
