package liaison.groble.domain.payment.entity;

import java.util.Map;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.common.utils.MapToJsonConverter;
import liaison.groble.domain.payment.enums.PaymentLogType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payment_logs",
    indexes = {
      @Index(name = "idx_payment_log_payment_id", columnList = "payment_id"),
      @Index(name = "idx_payment_log_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id")
  private Payment payment;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentLogType type;

  //  @Enumerated(EnumType.STRING)
  //  @Column(nullable = false)
  //  private Payment.PaymentStatus beforeStatus;
  //
  //  @Enumerated(EnumType.STRING)
  //  @Column(nullable = false)
  //  private Payment.PaymentStatus afterStatus;

  @Column(nullable = false)
  private String description;

  @Convert(converter = MapToJsonConverter.class)
  @Column(columnDefinition = "json")
  private Map<String, Object> requestData;

  @Convert(converter = MapToJsonConverter.class)
  @Column(columnDefinition = "json")
  private Map<String, Object> responseData;

  @Column private String ipAddress;

  @Column private String userAgent;

  // 빌더 패턴
  @Builder
  public PaymentLog(
      Payment payment,
      PaymentLogType type,
      //      Payment.PaymentStatus beforeStatus,
      //      Payment.PaymentStatus afterStatus,
      String description,
      Map<String, Object> requestData,
      Map<String, Object> responseData,
      String ipAddress,
      String userAgent) {
    this.payment = payment;
    this.type = type;
    //    this.beforeStatus = beforeStatus;
    //    this.afterStatus = afterStatus;
    this.description = description;
    this.requestData = requestData;
    this.responseData = responseData;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
  }
}
