package liaison.groble.domain.content.enums;

public enum AdminContentCheckingStatus {
  PENDING, // 모니터링 필요
  VALIDATED, // 모니터링 완료 && 승인됨
  REJECTED // 모니터링 완료 && 거절됨(판매중단)
}
