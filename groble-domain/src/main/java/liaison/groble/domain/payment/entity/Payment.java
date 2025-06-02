package liaison.groble.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.order.entity.Order;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payments"
    //    indexes = {
    //      @Index(name = "idx_payment_order", columnList = "order_id"),
    //      @Index(name = "idx_payment_pg_tid", columnList = "pg_tid"),
    //      @Index(name = "idx_payment_payment_key", columnList = "payment_key"),
    //      @Index(name = "idx_payment_status", columnList = "status"),
    //      @Index(name = "idx_payment_created_at", columnList = "created_at")
    )
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 주문 정보 (1:1 관계) - 하나의 주문에는 하나의 결제만 존재 - 주문 취소 시 결제도 함께 취소됨 */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  /** 취소 사유 - 결제 취소 요청 시 입력한 사유 - 관리자 또는 사용자가 입력 */
  @Column(name = "cancel_reason")
  private String cancelReason;
  //
  //    /**
  //     * PG사 고유 결제 키
  //     * - 페이플: PCD_PAY_OID
  //     * 결제 조회/취소 시 필수
  //     */
  //    @Column(name = "payment_key", unique = true)
  //    private String paymentKey;
  //
  //    /**
  //     * 가맹점 주문번호
  //     * - 우리 시스템에서 생성한 고유 주문번호
  //     * - Order.id 동일
  //     * - 중복 결제 방지용
  //     */
  //    @Column(name = "order_uid", nullable = false)
  //    private Long orderUid;
  //
  //    /**
  //     * PG사 거래 고유번호
  //     * - PG사에서 발급한 거래 식별자
  //     * - PCD_PAY_CARDTRADENUM (해당 거래의 고유 키)
  //     * - 카드사/은행 문의 시 필요
  //     */
  //    @Column(name = "pg_tid")
  //    private String pgTid;
  //
  //    /**
  //     * 결제 금액
  //     * - 실제 결제된 금액
  //     * - 쿠폰/포인트 차감 후 금액
  //     */
  //    @Column(nullable = false)
  //    private BigDecimal amount;
  //
  //    /**
  //     * 결제 상태
  //     * - READY: 결제 준비
  //     * - IN_PROGRESS: 결제 진행중
  //     * - WAITING_FOR_DEPOSIT: 가상계좌 입금 대기
  //     * - PAID: 결제 완료
  //     * - CANCELLED: 전체 취소
  //     * - PARTIALLY_CANCELLED: 부분 취소
  //     * - FAILED: 결제 실패
  //     */
  //    @Enumerated(EnumType.STRING)
  //    @Column(nullable = false)
  //    private PaymentStatus status = PaymentStatus.READY;
  //
  //    /**
  //     * 결제 수단
  //     * - CARD: 신용/체크카드
  //     * - BANK_TRANSFER: 계좌이체
  //     * - VIRTUAL_ACCOUNT: 가상계좌
  //     * - 간편결제: KAKAO_PAY, NAVER_PAY, TOSS_PAY 등
  //     */
  //    @Column(name = "payment_method", nullable = false)
  //    @Enumerated(EnumType.STRING)
  //    private PaymentMethod paymentMethod;
  //
  //    /**
  //     * 결제 수단 상세 정보
  //     * - 카드: "신한카드"
  //     * - 계좌이체: "국민은행"
  //     * - 간편결제: "카카오머니"
  //     */
  //    @Column(name = "method_detail")
  //    private String methodDetail;
  //
  //    /**
  //     * 구매자 이름
  //     * - 결제자 실명
  //     * - PG사 전달용
  //     */
  //    @Column(name = "customer_name")
  //    private String customerName;
  //
  //    /**
  //     * 구매자 이메일
  //     * - 결제 확인 메일 발송용
  //     * - 영수증 발급용
  //     */
  //    @Column(name = "customer_email")
  //    private String customerEmail;
  //
  //    /**
  //     * 구매자 휴대폰번호
  //     * - 결제 알림 SMS 발송용
  //     * - 본인인증용
  //     */
  //    @Column(name = "customer_mobile_phone")
  //    private String customerMobilePhone;
  //
  //    /**
  //     * 카드 정보 (전체)
  //     * - PG사에서 반환한 전체 카드 정보
  //     * - JSON 형태로 저장
  //     */
  //    @Column(name = "card_info")
  //    private String cardInfo;
  //
  //    /**
  //     * 카드번호 (마스킹)
  //     * - 예: 1234-****-****-5678
  //     * - 고객 문의 대응용
  //     */
  //    @Column(name = "card_number")
  //    private String cardNumber;
  //
  //    /**
  //     * 카드 유효기간 (년)
  //     */
  //    @Column(name = "card_expiry_year")
  //    private String cardExpiryYear;
  //
  //    /**
  //     * 카드 유효기간 (월)
  //     */
  //    @Column(name = "card_expiry_month")
  //    private String cardExpiryMonth;
  //
  //    /**
  //     * 카드 발급사 코드
  //     * - 예: "361" (BC카드)
  //     */
  //    @Column(name = "card_issuer_code")
  //    private String cardIssuerCode;
  //
  //    /**
  //     * 카드 발급사명
  //     * - 예: "BC카드"
  //     */
  //    @Column(name = "card_issuer_name")
  //    private String cardIssuerName;
  //
  //    /**
  //     * 카드 매입사 코드
  //     * - 실제 결제를 처리한 카드사
  //     */
  //    @Column(name = "card_acquirer_code")
  //    private String cardAcquirerCode;
  //
  //    /**
  //     * 카드 매입사명
  //     */
  //    @Column(name = "card_acquirer_name")
  //    private String cardAcquirerName;
  //
  //    /**
  //     * 할부 개월수
  //     * - "0": 일시불
  //     * - "3": 3개월 할부
  //     */
  //    @Column(name = "card_installment_plan_months")
  //    private String cardInstallmentPlanMonths;
  //
  //
  //    /**
  //     * 가상계좌번호
  //     * - 가상계좌 결제 시 발급
  //     * - 입금 확인용
  //     */
  //    @Column(name = "virtual_account_number")
  //    private String virtualAccountNumber;
  //
  //    /**
  //     * 가상계좌 은행코드
  //     * - 예: "004" (국민은행)
  //     */
  //    @Column(name = "virtual_account_bank_code")
  //    private String virtualAccountBankCode;
  //
  //    /**
  //     * 가상계좌 은행명
  //     * - 예: "국민은행"
  //     */
  //    @Column(name = "virtual_account_bank_name")
  //    private String virtualAccountBankName;
  //
  //    /**
  //     * 가상계좌 입금기한
  //     * - 기한 내 미입금 시 자동 취소
  //     */
  //    @Column(name = "virtual_account_expiry_date")
  //    private LocalDateTime virtualAccountExpiryDate;
  //
  //    /**
  //     * 영수증 URL
  //     * - 카드 매출전표
  //     * - 현금영수증
  //     * - 세금계산서
  //     */
  //    @Column(name = "receipt_url")
  //    private String receiptUrl;
  //
  //    /**
  //     * 에스크로 사용 여부
  //     * - 구매 안전 서비스
  //     * - 고액 거래 시 사용
  //     */
  //    @Column(name = "escrow")
  //    private boolean escrow;
  //
  //    /**
  //     * 현금영수증 발급 여부
  //     * - 현금성 결제 시 발급
  //     * - 세금 공제용
  //     */
  //    @Column(name = "cash_receipt")
  //    private boolean cashReceipt;
  //
  //    /**
  //     * 결제 완료 시각
  //     * - 실제 결제가 승인된 시각
  //     * - PG사 기준 시각
  //     */
  //    @Column(name = "paid_at")
  //    private LocalDateTime paidAt;
  //
  //    /**
  //     * 취소 사유
  //     * - 고객 변심
  //     * - 상품 품절
  //     * - 기타 사유
  //     */
  //    @Column(name = "cancel_reason")
  //    private String cancelReason;
  //
  //    /**
  //     * 취소 시각
  //     * - 취소가 완료된 시각
  //     */
  //    @Column(name = "cancel_at")
  //    private LocalDateTime cancelAt;
  //
  //    /**
  //     * 실패 사유
  //     * - 잔액 부족
  //     * - 한도 초과
  //     * - 카드 정지
  //     */
  //    @Column(name = "fail_reason")
  //    private String failReason;
  //
  //    /**
  //     * 결제 응답 파라미터
  //     * - PG사로부터 받은 모든 응답
  //     * - 결제 상세 정보 포함
  //     * - JSON 형태로 저장
  //     */
  //    @Convert(converter = MapToJsonConverter.class)
  //    @Column(name = "response_params", columnDefinition = "json")
  //    private Map<String, Object> responseParams;
  //
  //    /**
  //     * 메타 데이터
  //     * - 추가 정보 저장용
  //     * - 커스텀 필드
  //     * - JSON 형태로 저장
  //     */
  //    @Convert(converter = MapToJsonConverter.class)
  //    @Column(name = "meta_data", columnDefinition = "json")
  //    private Map<String, Object> metaData;
  //
  //    /**
  //     * 낙관적 락 버전
  //     * - 동시성 제어용
  //     * - 중복 결제 방지
  //     */
  //    @Version
  //    private Long version;
  //
  //    /**
  //     * 선택된 옵션 타입
  //     * - COACHING_OPTION: 코칭 옵션
  //     * - DOCUMENT_OPTION: 자료 옵션
  //     */
  //    @Column(name = "selected_option_type")
  //    @Enumerated(EnumType.STRING)
  //    private SelectedOptionType selectedOptionType;
  //
  //    /**
  //     * 선택된 옵션 ID
  //     * - 실제 구매한 옵션의 ID
  //     */
  //    @Column(name = "selected_option_id")
  //    private Long selectedOptionId;
  //
  //    /**
  //     * 결제 로그
  //     * - 결제 과정의 모든 이벤트 기록
  //     * - 상태 변경 이력
  //     */
  //    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
  //    private List<PaymentLog> logs = new ArrayList<>();
  //
  //    /**
  //     * 결제 취소 내역
  //     * - 부분 취소 지원
  //     * - 취소 건별 관리
  //     */
  //    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
  //    private List<PaymentCancel> cancellations = new ArrayList<>();
  //
  //    // 생성자
  //    @Builder
  //    public Payment(
  //            Order order,
  //            String paymentKey,
  //            PaymentMethod paymentMethod,
  //            BigDecimal amount,
  //            SelectedOptionType selectedOptionType,
  //            Long selectedOptionId,
  //            String customerName,
  //            String customerEmail,
  //            String customerMobilePhone) {
  //        this.order = order;
  //        this.paymentKey = paymentKey;
  //        this.paymentMethod = paymentMethod;
  //        this.amount = amount;
  //        this.selectedOptionType = selectedOptionType;
  //        this.selectedOptionId = selectedOptionId;
  //        this.customerName = customerName;
  //        this.customerEmail = customerEmail;
  //        this.customerMobilePhone = customerMobilePhone;
  //        this.merchantUid = order.getMerchantUid(); // Order로부터 가져옴
  //        order.setPayment(this);
  //    }
  //    /**
  //     * 결제 준비
  //     *
  //     * @param paymentKey PG사 결제 키
  //     * @param requestParams 요청 파라미터
  //     *
  //     * 호출 시점: 결제창 호출 전
  //     */
  //    public void preparePayment(String paymentKey, Map<String, Object> requestParams) {
  //        this.paymentKey = paymentKey;
  //        this.requestParams = requestParams;
  //        this.status = PaymentStatus.IN_PROGRESS;
  //
  //        addLog(
  //                PaymentLogType.PAYMENT_REQUESTED,
  //                PaymentStatus.READY,
  //                "결제 요청됨",
  //                requestParams,
  //                null,
  //                null,
  //                null
  //        );
  //    }
  //
  //    /**
  //     * 결제 완료 처리
  //     *
  //     * @param paymentKey PG사 결제 키
  //     * @param pgTid PG사 거래번호
  //     * @param responseParams 응답 파라미터
  //     *
  //     * 호출 시점: PG사 승인 완료 후
  //     */
  //    public void markAsPaid(String paymentKey, String pgTid, Map<String, Object> responseParams)
  // {
  //        if (this.status != PaymentStatus.READY && this.status != PaymentStatus.IN_PROGRESS) {
  //            throw new IllegalStateException("결제 준비 또는 진행 상태에서만 결제 완료 처리가 가능합니다.");
  //        }
  //
  //        PaymentStatus beforeStatus = this.status;
  //        this.paymentKey = paymentKey;
  //        this.pgTid = pgTid;
  //        this.responseParams = responseParams;
  //        this.status = PaymentStatus.PAID;
  //        this.paidAt = LocalDateTime.now();
  //
  //        // 주문 상태도 함께 업데이트
  //        this.order.completePayment();
  //
  //        addLog(
  //                PaymentLogType.PAYMENT_COMPLETED,
  //                beforeStatus,
  //                "결제 완료됨",
  //                null,
  //                responseParams,
  //                null,
  //                null
  //        );
  //    }
  //
  //    /**
  //     * 결제 실패 처리
  //     *
  //     * @param failReason 실패 사유
  //     * @param responseParams 응답 파라미터
  //     *
  //     * 호출 시점: PG사 오류 응답 시
  //     */
  //    public void markAsFailed(String failReason, Map<String, Object> responseParams) {
  //        if (this.status == PaymentStatus.PAID || this.status == PaymentStatus.CANCELLED) {
  //            throw new IllegalStateException("이미 완료되거나 취소된 결제는 실패 처리할 수 없습니다.");
  //        }
  //
  //        PaymentStatus beforeStatus = this.status;
  //        this.status = PaymentStatus.FAILED;
  //        this.failReason = failReason;
  //        this.responseParams = responseParams;
  //
  //        // 주문 상태도 함께 업데이트
  //        this.order.failOrder("결제 실패: " + failReason);
  //
  //        addLog(
  //                PaymentLogType.PAYMENT_FAILED,
  //                beforeStatus,
  //                "결제 실패: " + failReason,
  //                null,
  //                responseParams,
  //                null,
  //                null
  //        );
  //    }
  //
  //    /**
  //     * 결제 취소
  //     *
  //     * @param reason 취소 사유
  //     * @param cancelAmount 취소 금액
  //     * @return 생성된 취소 내역
  //     *
  //     * 호출 시점: 환불 요청 시
  //     */
  //    public PaymentCancel cancel(String reason, BigDecimal cancelAmount) {
  //        if (this.status != PaymentStatus.PAID && this.status !=
  // PaymentStatus.PARTIALLY_CANCELLED) {
  //            throw new IllegalStateException("결제 완료 또는 부분 취소 상태에서만 취소가 가능합니다.");
  //        }
  //
  //        // 이미 취소된 금액 계산
  //        BigDecimal totalCancelledAmount = cancellations.stream()
  //                .filter(c -> c.getStatus() == PaymentCancelStatus.COMPLETED)
  //                .map(PaymentCancel::getAmount)
  //                .reduce(BigDecimal.ZERO, BigDecimal::add);
  //
  //        // 취소 가능 금액 확인
  //        BigDecimal remainingAmount = this.amount.subtract(totalCancelledAmount);
  //        if (cancelAmount.compareTo(remainingAmount) > 0) {
  //            throw new IllegalArgumentException("취소 금액이 남은 결제 금액보다 큽니다.");
  //        }
  //
  //        // 부분 취소인지 전체 취소인지 확인
  //        boolean isPartialCancel = cancelAmount.compareTo(remainingAmount) < 0;
  //
  //        PaymentStatus beforeStatus = this.status;
  //        if (isPartialCancel) {
  //            this.status = PaymentStatus.PARTIALLY_CANCELLED;
  //        } else {
  //            this.status = PaymentStatus.CANCELLED;
  //            this.cancelAt = LocalDateTime.now();
  //        }
  //
  //        this.cancelReason = reason;
  //
  //        // 주문 상태도 함께 업데이트 (전체 취소일 때만)
  //        if (!isPartialCancel) {
  //            this.order.cancelOrder("결제 취소: " + reason);
  //        }
  //
  //        // 취소 내역 생성
  //        PaymentCancel paymentCancel = PaymentCancel.builder()
  //                .payment(this)
  //                .amount(cancelAmount)
  //                .reason(reason)
  //                .status(PaymentCancelStatus.REQUESTED)
  //                .build();
  //
  //        this.cancellations.add(paymentCancel);
  //
  //        addLog(
  //                PaymentLogType.CANCELLATION_REQUESTED,
  //                beforeStatus,
  //                "결제 취소 요청: " + reason,
  //                Map.of("cancelAmount", cancelAmount),
  //                null,
  //                null,
  //                null
  //        );
  //
  //        return paymentCancel;
  //    }
  //
  //    /**
  //     * 카드 정보 설정
  //     *
  //     * 호출 시점: 카드 결제 완료 후
  //     */
  //    public void setCardInfo(
  //            String cardInfo,
  //            String cardNumber,
  //            String cardExpiryYear,
  //            String cardExpiryMonth,
  //            String cardIssuerCode,
  //            String cardIssuerName,
  //            String cardAcquirerCode,
  //            String cardAcquirerName,
  //            String cardInstallmentPlanMonths) {
  //        this.cardInfo = cardInfo;
  //        this.cardNumber = cardNumber;
  //        this.cardExpiryYear = cardExpiryYear;
  //        this.cardExpiryMonth = cardExpiryMonth;
  //        this.cardIssuerCode = cardIssuerCode;
  //        this.cardIssuerName = cardIssuerName;
  //        this.cardAcquirerCode = cardAcquirerCode;
  //        this.cardAcquirerName = cardAcquirerName;
  //        this.cardInstallmentPlanMonths = cardInstallmentPlanMonths;
  //        this.methodDetail = cardIssuerName; // 카드사명을 상세 정보로 저장
  //    }
  //
  //    /**
  //     * 가상계좌 정보 설정
  //     *
  //     * 호출 시점: 가상계좌 발급 후
  //     */
  //    public void setVirtualAccountInfo(
  //            String accountNumber,
  //            String bankCode,
  //            String bankName,
  //            LocalDateTime expiryDate) {
  //        this.virtualAccountNumber = accountNumber;
  //        this.virtualAccountBankCode = bankCode;
  //        this.virtualAccountBankName = bankName;
  //        this.virtualAccountExpiryDate = expiryDate;
  //        this.methodDetail = bankName; // 은행명을 상세 정보로 저장
  //        this.status = PaymentStatus.WAITING_FOR_DEPOSIT;
  //    }
  //
  //    /**
  //     * 로그 추가
  //     *
  //     * 결제 과정의 모든 이벤트를 기록
  //     */
  //    public void addLog(
  //            PaymentLogType type,
  //            PaymentStatus beforeStatus,
  //            String description,
  //            Map<String, Object> requestData,
  //            Map<String, Object> responseData,
  //            String ipAddress,
  //            String userAgent) {
  //        PaymentLog log = PaymentLog.builder()
  //                .payment(this)
  //                .type(type)
  //                .beforeStatus(beforeStatus)
  //                .afterStatus(this.status)
  //                .description(description)
  //                .requestData(requestData)
  //                .responseData(responseData)
  //                .ipAddress(ipAddress)
  //                .userAgent(userAgent)
  //                .build();
  //
  //        this.logs.add(log);
  //    }
  //
  //    // 나머지 메서드들...
  //    public void setReceiptUrl(String receiptUrl) {
  //        this.receiptUrl = receiptUrl;
  //    }
  //
  //    public void setEscrow(boolean escrow) {
  //        this.escrow = escrow;
  //    }
  //
  //    public void setCashReceipt(boolean cashReceipt) {
  //        this.cashReceipt = cashReceipt;
  //    }
  //
  //    public void markAsWaitingForDeposit() {
  //        if (this.paymentMethod != PaymentMethod.VIRTUAL_ACCOUNT) {
  //            throw new IllegalStateException("가상계좌 결제만 입금 대기 상태로 변경 가능합니다.");
  //        }
  //        this.status = PaymentStatus.WAITING_FOR_DEPOSIT;
  //    }
  //
  //    public void updateStatus(PaymentStatus status) {
  //        PaymentStatus beforeStatus = this.status;
  //        this.status = status;
  //
  //        addLog(
  //                PaymentLogType.STATUS_CHANGED,
  //                beforeStatus,
  //                "상태 변경: " + beforeStatus + " -> " + status,
  //                null,
  //                null,
  //                null,
  //                null
  //        );
  //    }
  //
  //    /**
  //     * PG사별 페이플 결제 정보와 매핑
  //     *
  //     * 페이플 결제 완료 후 Payment 엔티티 생성 예시:
  //     * Payment payment = Payment.builder()
  //     *     .order(order)
  //     *     .paymentKey(payplePayment.getOrderId()) // PCD_PAY_OID
  //     *     .paymentMethod(PaymentMethod.CARD)
  //     *     .amount(order.getFinalAmount())
  //     *     .customerName(authResult.getPayerName())
  //     *     .customerEmail(authResult.getPayerEmail())
  //     *     .customerMobilePhone(authResult.getPayerHp())
  //     *     .build();
  //     *
  //     * payment.markAsPaid(
  //     *     payplePayment.getOrderId(),
  //     *     approvalResult.get("PCD_PAY_CARDTRADENUM"), // PG TID
  //     *     approvalResult
  //     * );
  //     *
  //     * payment.setCardInfo(
  //     *     null,
  //     *     approvalResult.get("PCD_PAY_CARDNUM"),
  //     *     null, null,
  //     *     null,
  //     *     approvalResult.get("PCD_PAY_CARDNAME"),
  //     *     null, null,
  //     *     approvalResult.get("PCD_PAY_CARDQUOTA")
  //     * );
  //     */
  //
  //    // 열거형들은 원본과 동일...
  //    public enum PaymentStatus {
  //        READY("결제준비"),
  //        IN_PROGRESS("결제진행중"),
  //        WAITING_FOR_DEPOSIT("입금대기중"),
  //        PAID("결제완료"),
  //        CANCELLED("취소됨"),
  //        PARTIALLY_CANCELLED("부분취소됨"),
  //        ABORTED("결제중단"),
  //        FAILED("결제실패"),
  //        EXPIRED("결제만료");
  //
  //        private final String description;
  //
  //        PaymentStatus(String description) {
  //            this.description = description;
  //        }
  //
  //        public String getDescription() {
  //            return description;
  //        }
  //    }
  //
  //    public enum PgProvider {
  //        INICIS("KG이니시스"),
  //        KAKAO("카카오페이"),
  //        TOSS("토스페이먼츠"),
  //        NAVER("네이버페이"),
  //        PAYPLE("페이플");
  //
  //        private final String description;
  //
  //        PgProvider(String description) {
  //            this.description = description;
  //        }
  //
  //        public String getDescription() {
  //            return description;
  //        }
  //    }
  //
  //    public enum PaymentMethod {
  //        CARD("신용카드"),
  //        BANK_TRANSFER("계좌이체"),
  //        VIRTUAL_ACCOUNT("가상계좌"),
  //        MOBILE_PHONE("휴대폰결제"),
  //        CULTURE_VOUCHER("문화상품권"),
  //        GIFT_CERTIFICATE("상품권"),
  //        KAKAO_PAY("카카오페이"),
  //        PAYCO("페이코"),
  //        NAVER_PAY("네이버페이"),
  //        SAMSUNG_PAY("삼성페이"),
  //        TOSS_PAY("토스페이"),
  //        POINT("포인트결제"),
  //        PAYPAL("페이팔"),
  //        ALIPAY("알리페이"),
  //        WECHAT_PAY("위챗페이"),
  //        OVERSEAS_CARD("해외카드");
  //
  //        private final String description;
  //
  //        PaymentMethod(String description) {
  //            this.description = description;
  //        }
  //
  //        public String getDescription() {
  //            return description;
  //        }
  //    }
  //
  //    public enum SelectedOptionType {
  //        COACHING_OPTION,
  //        DOCUMENT_OPTION
  //    }
}
