package liaison.groble.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.common.utils.MapToJsonConverter;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.enums.PaymentCancelStatus;
import liaison.groble.domain.payment.enums.PaymentLogType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payments",
    indexes = {
      @Index(name = "idx_payment_order", columnList = "order_id"),
      @Index(name = "idx_payment_pg_tid", columnList = "pg_tid"),
      @Index(name = "idx_payment_payment_key", columnList = "payment_key"),
      @Index(name = "idx_payment_status", columnList = "status"),
      @Index(name = "idx_payment_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  // 포트원 고유 결제 키
  @Column(name = "payment_key", unique = true)
  private String paymentKey;

  // 가맹점 주문번호
  @Column(name = "merchant_uid", nullable = false)
  private String merchantUid;

  @Column(name = "pg_provider", nullable = false)
  @Enumerated(EnumType.STRING)
  private PgProvider pgProvider = PgProvider.PORTONE;

  @Column(name = "pg_tid")
  private String pgTid; // PG사 거래 고유번호

  @Column(nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status = PaymentStatus.READY;

  @Column(name = "payment_method", nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  // 결제 수단 상세 정보 (카드사, 은행명 등)
  @Column(name = "method_detail")
  private String methodDetail;

  // 구매자 정보
  @Column(name = "customer_name")
  private String customerName;

  @Column(name = "customer_email")
  private String customerEmail;

  @Column(name = "customer_mobile_phone")
  private String customerMobilePhone;

  // 카드 정보
  @Column(name = "card_info")
  private String cardInfo;

  @Column(name = "card_number")
  private String cardNumber; // 마스킹 처리됨

  @Column(name = "card_expiry_year")
  private String cardExpiryYear;

  @Column(name = "card_expiry_month")
  private String cardExpiryMonth;

  @Column(name = "card_issuer_code")
  private String cardIssuerCode;

  @Column(name = "card_issuer_name")
  private String cardIssuerName;

  @Column(name = "card_acquirer_code")
  private String cardAcquirerCode;

  @Column(name = "card_acquirer_name")
  private String cardAcquirerName;

  @Column(name = "card_installment_plan_months")
  private String cardInstallmentPlanMonths;

  // 가상계좌 정보
  @Column(name = "virtual_account_number")
  private String virtualAccountNumber;

  @Column(name = "virtual_account_bank_code")
  private String virtualAccountBankCode;

  @Column(name = "virtual_account_bank_name")
  private String virtualAccountBankName;

  @Column(name = "virtual_account_expiry_date")
  private LocalDateTime virtualAccountExpiryDate;

  // 영수증 정보
  @Column(name = "receipt_url")
  private String receiptUrl;

  @Column(name = "escrow")
  private boolean escrow;

  @Column(name = "cash_receipt")
  private boolean cashReceipt;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  @Column(name = "cancel_reason")
  private String cancelReason;

  @Column(name = "cancel_at")
  private LocalDateTime cancelAt;

  @Column(name = "fail_reason")
  private String failReason;

  // 포트원 로그 추적
  @Column(name = "port_one_request_id")
  private String portOneRequestId;

  @Column(name = "port_one_webhook_id")
  private String portOneWebhookId;

  // 요청/응답 데이터를 JSON으로 저장
  @Convert(converter = MapToJsonConverter.class)
  @Column(name = "request_params", columnDefinition = "json")
  private Map<String, Object> requestParams;

  @Convert(converter = MapToJsonConverter.class)
  @Column(name = "response_params", columnDefinition = "json")
  private Map<String, Object> responseParams;

  @Convert(converter = MapToJsonConverter.class)
  @Column(name = "meta_data", columnDefinition = "json")
  private Map<String, Object> metaData;

  @Version private Long version;

  // 선택된 옵션 정보
  @Column(name = "selected_option_type")
  @Enumerated(EnumType.STRING)
  private SelectedOptionType selectedOptionType;

  @Column(name = "selected_option_id")
  private Long selectedOptionId;

  // 결제 로그 및 취소 이력
  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PaymentLog> logs = new ArrayList<>();

  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PaymentCancel> cancellations = new ArrayList<>();

  // 생성자
  @Builder
  public Payment(
      Order order,
      PaymentMethod paymentMethod,
      BigDecimal amount,
      SelectedOptionType selectedOptionType,
      Long selectedOptionId,
      String customerName,
      String customerEmail,
      String customerMobilePhone) {
    this.order = order;
    this.paymentMethod = paymentMethod;
    this.amount = amount;
    this.selectedOptionType = selectedOptionType;
    this.selectedOptionId = selectedOptionId;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.customerMobilePhone = customerMobilePhone;
    this.merchantUid = order.getMerchantUid();
    order.setPayment(this);
  }

  // 비즈니스 메서드
  public void preparePayment(String paymentKey, Map<String, Object> requestParams) {
    this.paymentKey = paymentKey;
    this.requestParams = requestParams;
  }

  public void markAsPaid(String paymentKey, String pgTid, Map<String, Object> responseParams) {
    if (this.status != PaymentStatus.READY) {
      throw new IllegalStateException("결제 준비 상태에서만 결제 완료 처리가 가능합니다.");
    }

    this.paymentKey = paymentKey;
    this.pgTid = pgTid;
    this.responseParams = responseParams;
    this.status = PaymentStatus.PAID;
    this.paidAt = LocalDateTime.now();

    // 주문 상태도 함께 업데이트
    this.order.completePayment();
  }

  public void markAsFailed(String failReason, Map<String, Object> responseParams) {
    if (this.status != PaymentStatus.READY) {
      throw new IllegalStateException("결제 준비 상태에서만 결제 실패 처리가 가능합니다.");
    }

    this.status = PaymentStatus.FAILED;
    this.failReason = failReason;
    this.responseParams = responseParams;

    // 주문 상태도 함께 업데이트
    this.order.failOrder("결제 실패: " + failReason);
  }

  public PaymentCancel cancel(String reason, BigDecimal cancelAmount) {
    if (this.status != PaymentStatus.PAID) {
      throw new IllegalStateException("결제 완료 상태에서만 취소가 가능합니다.");
    }

    // 부분 취소인지 전체 취소인지 확인
    boolean isPartialCancel = cancelAmount.compareTo(this.amount) < 0;

    if (isPartialCancel) {
      this.status = PaymentStatus.PARTIALLY_CANCELLED;
    } else {
      this.status = PaymentStatus.CANCELLED;
    }

    this.cancelReason = reason;
    this.cancelAt = LocalDateTime.now();

    // 주문 상태도 함께 업데이트
    if (!isPartialCancel) {
      this.order.cancelOrder("결제 취소: " + reason);
    }

    // 취소 내역 생성
    PaymentCancel paymentCancel =
        PaymentCancel.builder()
            .payment(this)
            .amount(cancelAmount)
            .reason(reason)
            .status(PaymentCancelStatus.REQUESTED)
            .build();

    this.cancellations.add(paymentCancel);
    return paymentCancel;
  }

  public void setCardInfo(
      String cardInfo,
      String cardNumber,
      String cardExpiryYear,
      String cardExpiryMonth,
      String cardIssuerCode,
      String cardIssuerName,
      String cardAcquirerCode,
      String cardAcquirerName,
      String cardInstallmentPlanMonths) {
    this.cardInfo = cardInfo;
    this.cardNumber = cardNumber;
    this.cardExpiryYear = cardExpiryYear;
    this.cardExpiryMonth = cardExpiryMonth;
    this.cardIssuerCode = cardIssuerCode;
    this.cardIssuerName = cardIssuerName;
    this.cardAcquirerCode = cardAcquirerCode;
    this.cardAcquirerName = cardAcquirerName;
    this.cardInstallmentPlanMonths = cardInstallmentPlanMonths;
  }

  public void setVirtualAccountInfo(
      String accountNumber, String bankCode, String bankName, LocalDateTime expiryDate) {
    this.virtualAccountNumber = accountNumber;
    this.virtualAccountBankCode = bankCode;
    this.virtualAccountBankName = bankName;
    this.virtualAccountExpiryDate = expiryDate;
  }

  public void addLog(
      PaymentLogType type,
      PaymentStatus beforeStatus,
      String description,
      Map<String, Object> requestData,
      Map<String, Object> responseData,
      String ipAddress,
      String userAgent) {
    PaymentLog log =
        PaymentLog.builder()
            .payment(this)
            .type(type)
            .beforeStatus(beforeStatus)
            .afterStatus(this.status)
            .description(description)
            .requestData(requestData)
            .responseData(responseData)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

    this.logs.add(log);
  }

  // 웹훅 정보 업데이트
  public void updateFromWebhook(Map<String, Object> webhookData) {
    this.portOneWebhookId = (String) webhookData.get("webhookId");
    this.metaData = webhookData;
  }

  // 열거형
  public enum PaymentStatus {
    READY("결제준비"),
    IN_PROGRESS("결제진행중"),
    WAITING_FOR_DEPOSIT("입금대기중"),
    PAID("결제완료"),
    CANCELLED("취소됨"),
    PARTIALLY_CANCELLED("부분취소됨"),
    ABORTED("결제중단"),
    FAILED("결제실패");

    private final String description;

    PaymentStatus(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum PgProvider {
    PORTONE("포트원"),
    INICIS("KG이니시스"),
    KAKAO("카카오페이"),
    TOSS("토스페이먼츠"),
    NAVER("네이버페이");

    private final String description;

    PgProvider(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum PaymentMethod {
    CARD("신용카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PHONE("휴대폰결제"),
    CULTURE_VOUCHER("문화상품권"),
    GIFT_CERTIFICATE("상품권"),
    KAKAO_PAY("카카오페이"),
    PAYCO("페이코"),
    NAVER_PAY("네이버페이"),
    SAMSUNG_PAY("삼성페이"),
    TOSS_PAY("토스페이"),
    POINT("포인트결제"),
    PAYPAL("페이팔"),
    ALIPAY("알리페이"),
    WECHAT_PAY("위챗페이"),
    OVERSEAS_CARD("해외카드");

    private final String description;

    PaymentMethod(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  // 옵션 타입 enum
  public enum SelectedOptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
