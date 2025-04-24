package liaison.groble.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.*;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.common.utils.MapToJsonConverter;
import liaison.groble.domain.payment.enums.PaymentCancelStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_cancellations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancel extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @Column private String cancelKey; // 포트원 취소 키

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private String reason;

  @Column private String taxFreeAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentCancelStatus status;

  @Column private LocalDateTime cancelledAt;

  @Convert(converter = MapToJsonConverter.class)
  @Column(columnDefinition = "json")
  private Map<String, Object> metaData;

  @Builder
  public PaymentCancel(
      Payment payment, BigDecimal amount, String reason, PaymentCancelStatus status) {
    this.payment = payment;
    this.amount = amount;
    this.reason = reason;
    this.status = status;
  }

  public void complete(String cancelKey, Map<String, Object> metaData) {
    this.cancelKey = cancelKey;
    this.metaData = metaData;
    this.status = PaymentCancelStatus.COMPLETED;
    this.cancelledAt = LocalDateTime.now();
  }

  public void fail(String errorMessage) {
    this.status = PaymentCancelStatus.FAILED;
    if (this.metaData == null) {
      this.metaData = Map.of("errorMessage", errorMessage);
    } else {
      this.metaData.put("errorMessage", errorMessage);
    }
  }
}
