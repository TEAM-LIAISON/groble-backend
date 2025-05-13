package liaison.groble.domain.chat.enums;

/** 채팅방의 상태를 나타내는 열거형 채팅방의 라이프사이클 관리에 사용됨 */
public enum ChatRoomStatus {
  ACTIVE, // 활성화된 채팅방
  INACTIVE, // 일정 기간 메시지가 없어 비활성화된 채팅방
  COMPLETED, // 거래가 완료된 채팅방
  BLOCKED, // 사용자가 차단한 채팅방
  DELETED // 삭제된 채팅방
}
