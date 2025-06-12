package liaison.groble.domain.content.enums;

public enum ContentStatus {
  DRAFT, // 초안 (작성중)
  PENDING, // 검토 중 (심사중)
  VALIDATED, // 승인됨 (관리자 승인 완료, 판매 준비 상태)
  ACTIVE, // 활성화 (판매중)
  REJECTED, // 거절됨 (관리자 승인 거절)
  DELETED, // 삭제됨

  // TODO: 판매 중단 상태 추가 필요
  // 검토 중, 승인됨 제거 필요
  DISCONTINUED // 판매 중단 (판매 중단 상태)
}
