package liaison.groble.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payple_payments",
    indexes = {
      @Index(name = "idx_payple_payment_order_id", columnList = "order_id"),
      @Index(name = "idx_payple_payment_user_id", columnList = "user_id"),
      @Index(name = "idx_payple_payment_billing_key", columnList = "billing_key"),
      @Index(name = "idx_payple_payment_status", columnList = "status"),
      @Index(name = "idx_payple_payment_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayplePayment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PayplePaymentStatus status = PayplePaymentStatus.PENDING;

  // 페이플 인증 결과
  private String pcdPayRst;

  // 페이플 결제 응답 코드
  private String pcdPayCode;

  // 페이플 응답 메시지
  private String pcdPayMsg;

  // 페이플 결제수단(카드/계좌) card/transfer
  private String pcdPayType;

  // 승인 요청 결제키
  private String pcdPayReqKey;

  // 주문번호 (파트너에서 미전송 시 페이플에서 발급한 주문번호 응답)
  private String pcdPayOid;

  // 그로블에서 이용하는 회원번호
  private String pcdPayerNo;

  // 구매자 이름
  private String pcdPayerName;

  // 구매자 핸드폰번호
  private String pcdPayerHp;

  // 구매자 이메일
  private String pcdPayerEmail;

  // 상품명
  private String pcdPayGoods;

  // 총 결제금액
  private String pcdPayTotal;

  // 복합과세 부가세
  private String pcdPayTaxTotal;

  // 과세 여부
  private String pcdPayIsTax;

  // 결제 요청 시간
  private String pcdPayTime;

  // 카드사명
  private String pcdPayCardName;

  // 카드번호
  private String pcdPayCardNum;

  // 해당 거래의 고유 키
  private String pcdPayCardTradeNum;

  // 승인번호
  private String pcdPayCardAuthNo;

  // 매출 전표(영수증) 출력 URL
  private String pcdPayCardReceipt;

  // 정기(빌링), 비밀번호 간편결제 시 필요한 설정값
  private String pcdSimpleFlag;

  // 파트너에서 입력한 값 1
  private String pcdUserDefine1;

  // 파트너에서 입력한 값 2
  private String pcdUserDefine2;

  /** 결제 완료 시각 (PCD_PAY_TIME) - 실제 결제가 승인된 시각 - 형식: yyyyMMddHHmmss */
  @Column(name = "payment_date")
  private LocalDateTime paymentDate;

  /** 실패 사유 (PCD_PAY_MSG) - 결제 실패 시 페이플에서 반환하는 메시지 - 사용자에게 보여줄 수 있는 친화적인 메시지 */
  @Column(name = "fail_reason")
  private String failReason;

  /** 취소 사유 - 결제 취소 요청 시 입력한 사유 - 관리자 또는 사용자가 입력 */
  @Column(name = "cancel_reason")
  private String cancelReason;

  /** 취소 시각 - 결제가 취소된 시각 - 환불 처리 추적에 사용 */
  @Column(name = "canceled_at")
  private LocalDateTime canceledAt;
}
