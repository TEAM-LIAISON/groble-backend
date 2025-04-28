package liaison.groble.domain.content.enums;

public enum ContentStatus {
  DRAFT, // 초안 (작성중)
  PENDING, // 검토 중 (심사중)
  APPROVED, // 심사완료 (심사완료)
  ACTIVE, // 활성화 (판매중)
  INACTIVE, // 비활성화 (판매자가 일시 중단)
  REJECTED, // 거절됨 (관리자 승인 거절)
  DELETED // 삭제됨
}
