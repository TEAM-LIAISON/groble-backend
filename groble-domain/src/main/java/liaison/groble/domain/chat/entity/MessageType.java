package liaison.groble.domain.chat.entity;

/** 메시지 유형을 나타내는 열거형 다양한 메시지 종류를 구분하기 위해 사용 */
public enum MessageType {
  TEXT, // 일반 텍스트 메시지
  IMAGE, // 이미지 메시지
  FILE, // 파일 첨부
  LOCATION, // 위치 정보
  PRICE_OFFER, // 가격 제안
  SYSTEM, // 시스템 메시지 (알림 등)
  DEAL_REQUEST, // 거래 요청
  DEAL_ACCEPTED, // 거래 수락
  DEAL_REJECTED // 거래 거절
}
