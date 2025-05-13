package liaison.groble.domain.payment.enums;

public enum PaymentLogType {
  PAYMENT_CREATED, // 결제 생성
  PAYMENT_REQUESTED, // 결제 요청
  PAYMENT_APPROVED, // 결제 승인
  PAYMENT_CANCEL_REQUESTED, // 결제 취소 요청
  PAYMENT_CANCELED, // 결제 취소 완료
  VIRTUAL_ACCOUNT_ISSUED, // 가상계좌 발급
  VIRTUAL_ACCOUNT_DEPOSIT, // 가상계좌 입금
  WEBHOOK_RECEIVED, // 웹훅 수신
  ESCROW_RECEIVED, // 에스크로 구매 확정
  STATUS_CHANGED, // 상태 변경
  ERROR // 오류 발생
}
