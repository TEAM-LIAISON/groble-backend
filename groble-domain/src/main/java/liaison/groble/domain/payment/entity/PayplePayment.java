package liaison.groble.domain.payment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payple_payments",
    indexes = {
      @Index(name = "idx_pcd_pay_oid", columnList = "pcd_pay_oid"),
      @Index(name = "idx_pcd_payer_no", columnList = "pcd_payer_no"),
      @Index(name = "idx_payple_payment_created_at", columnList = "created_at")
    })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PayplePayment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pcdPayRst; // 페이플 인증 결과 (SUCCESS/ERROR/CLOSE)
  private String pcdPayCode; // 페이플 결제 응답 코드 (0000)
  private String pcdPayMsg; // 페이플 응답 메시지
  private String pcdPayType; // 페이플 결제수단(카드/계좌) card/transfer
  private String pcdPayCardVer; // 카드 인증 버전 (02 등)
  private String pcdPayWork; // 승인 요청 방식 결과 (CERT 등)

  @Column(name = "pcd_pay_auth_key", length = 512)
  private String pcdPayAuthKey;

  @Column(name = "pcd_pay_req_key", length = 1024)
  private String pcdPayReqKey;

  @Column(name = "pcd_pay_host", length = 1024)
  private String pcdPayHost;

  @Column(name = "pcd_pay_cofurl", length = 1024)
  private String pcdPayCofUrl;

  @Column(name = "pcd_pay_oid", unique = true)
  private String pcdPayOid; // 주문번호 (orderId - PK)

  @Column(name = "pcd_easy_pay_method")
  private String pcdEasyPayMethod; // 선택한 결제 수단의 상세 유형 (카드, 계좌이체 등)

  private String pcdPayerId; // 빌링키 (정기결제용)
  private String pcdPayerNo; // 그로블에서 이용하는 회원번호 (userId - PK)
  private String pcdPayerName; // 구매자 이름
  private String pcdPayerHp; // 구매자 핸드폰번호
  private String pcdPayerEmail; // 구매자 이메일
  private String pcdPayGoods; // 상품명
  private String pcdPayTotal; // 총 결제금액
  private String pcdPayTaxTotal; // 복합과세 부가세
  private String pcdPayIsTax; // 과세 여부

  private String pcdPayCardName; // 카드사명

  @Column(name = "pcd_pay_card_num")
  private String pcdPayCardNum; // 마스킹된 상태로 저장돼야 함

  private String pcdPayCardQuota; // 할부 개월 수
  private String pcdPayCardTradeNum; // 해당 거래의 고유 키
  private String pcdPayCardAuthNo; // 승인번호
  private String pcdPayCardReceipt; // 매출 전표(영수증) 출력 URL
  private String pcdPayTime; // 결제 요청 시간
  private String pcdRegulerFlag; // 월 중복방지 거래 설정 여부
  private String pcdPayYear; // 월 중복방지 거래 - 년(Year)
  private String pcdPayMonth; // 월 중복방지 거래 - 월(Month)
  private String pcdSimpleFlag; // 정기(빌링), 비밀번호 간편결제 시 필요한 설정값
  private String pcdRstUrl; // 결제 정보 인증 결과가 POST 방식으로 전송되는 경로

  private String pcdUserDefine1; // 파트너에서 입력한 값 1
  private String pcdUserDefine2; // 파트너에서 입력한 값 2
  private LocalDateTime paymentDate; // 결제 완료 시간
  private LocalDateTime canceledAt; // 결제 취소 시간

  private String failReason;
  private String cancelReason;

  // 결제 취소
  public void cancel(String cancelReason, LocalDateTime cancelTime) {
    this.cancelReason = cancelReason;
    this.canceledAt = cancelTime != null ? cancelTime : LocalDateTime.now();
  }

  // 승인 정보 업데이트
  public void updateApprovalInfo(
      String payTime,
      String cardName,
      String cardNum,
      String cardTradeNum,
      String cardAuthNo,
      String receiptUrl) {
    this.pcdPayTime = payTime;
    this.pcdPayCardName = cardName;
    this.pcdPayCardNum = cardNum;
    this.pcdPayCardTradeNum = cardTradeNum;
    this.pcdPayCardAuthNo = cardAuthNo;
    this.pcdPayCardReceipt = receiptUrl;
    this.paymentDate = LocalDateTime.now();
  }
}
