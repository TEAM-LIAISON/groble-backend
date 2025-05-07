package liaison.groble.domain.content.enums;

public enum ContentStatus {
  DRAFT, // 초안 (작성중)
  PENDING, // 검토 중 (심사중)
  VALIDATED, // 승인됨 (관리자 승인 완료, 판매 준비 상태)
  ACTIVE, // 활성화 (판매중)
  INACTIVE, // 비활성화 (판매자가 일시 중단)
  REJECTED, // 거절됨 (관리자 승인 거절)
  DELETED // 삭제됨
}
