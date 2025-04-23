package liaison.groble.domain.user.entity;

import java.util.Map;

import jakarta.persistence.*;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.common.utils.MapToJsonConverter;
import liaison.groble.domain.user.enums.IdentityVerificationStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "identity_verification_history",
    indexes = {
      @Index(name = "idx_identity_verification_history_user", columnList = "user_id"),
      @Index(name = "idx_identity_verification_history_txid", columnList = "transaction_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentityVerificationHistory extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "request_ip", nullable = false)
  private String requestIp;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "verification_method", nullable = false)
  @Enumerated(EnumType.STRING)
  private IdentityVerification.VerificationMethod verificationMethod;

  @Column(name = "before_status")
  @Enumerated(EnumType.STRING)
  private IdentityVerificationStatus beforeStatus;

  @Column(name = "after_status")
  @Enumerated(EnumType.STRING)
  private IdentityVerificationStatus afterStatus;

  @Column(name = "success")
  private boolean success;

  @Column(name = "transaction_id")
  private String transactionId;

  @Column(name = "port_one_request_id")
  private String portOneRequestId;

  @Column(name = "error_code")
  private String errorCode;

  @Column(name = "error_message")
  private String errorMessage;

  @Convert(converter = MapToJsonConverter.class)
  @Column(name = "raw_request", columnDefinition = "json")
  private Map<String, Object> rawRequest;

  @Convert(converter = MapToJsonConverter.class)
  @Column(name = "raw_response", columnDefinition = "json")
  private Map<String, Object> rawResponse;

  @Builder
  public IdentityVerificationHistory(
      User user,
      String requestIp,
      String userAgent,
      IdentityVerification.VerificationMethod verificationMethod,
      IdentityVerificationStatus beforeStatus,
      String transactionId,
      String portOneRequestId,
      Map<String, Object> rawRequest) {
    this.user = user;
    this.requestIp = requestIp;
    this.userAgent = userAgent;
    this.verificationMethod = verificationMethod;
    this.beforeStatus = beforeStatus;
    this.transactionId = transactionId;
    this.portOneRequestId = portOneRequestId;
    this.rawRequest = rawRequest;
    this.success = false; // 초기값
  }

  public void markAsSuccess(
      IdentityVerificationStatus afterStatus, Map<String, Object> rawResponse) {
    this.success = true;
    this.afterStatus = afterStatus;
    this.rawResponse = rawResponse;
  }

  public void markAsFailure(
      IdentityVerificationStatus afterStatus,
      String errorCode,
      String errorMessage,
      Map<String, Object> rawResponse) {
    this.success = false;
    this.afterStatus = afterStatus;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.rawResponse = rawResponse;
  }
}
