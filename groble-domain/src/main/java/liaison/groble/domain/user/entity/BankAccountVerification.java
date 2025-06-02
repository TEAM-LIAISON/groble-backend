package liaison.groble.domain.user.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.common.utils.MapToJsonConverter;
import liaison.groble.domain.user.enums.BankAccountVerificationStatus;
import liaison.groble.domain.user.enums.BankAccountVerificationType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bank_account_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BankAccountVerification extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bank_account_id")
  private BankAccount bankAccount;

  @Column(nullable = false)
  private String verificationKey; // 포트원 계좌 인증 요청 키

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BankAccountVerificationType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BankAccountVerificationStatus status;

  @Column private BigDecimal depositPrice; // 1원 인증 시 입금액

  @Column private String depositBank; // 1원 인증 시 출금 은행

  @Column private String depositAccountNumber; // 1원 인증 시 출금 계좌번호 (마스킹)

  @Column private LocalDateTime expiredAt; // 인증 만료 시간

  @Column private LocalDateTime verifiedAt; // 인증 완료 시간

  @Convert(converter = MapToJsonConverter.class)
  @Column(columnDefinition = "json")
  private Map<String, Object> requestData;

  @Convert(converter = MapToJsonConverter.class)
  @Column(columnDefinition = "json")
  private Map<String, Object> responseData;

  @Builder
  public BankAccountVerification(
      BankAccount bankAccount,
      String verificationKey,
      BankAccountVerificationStatus status,
      BankAccountVerificationType type,
      Map<String, Object> requestData) {
    this.bankAccount = bankAccount;
    this.verificationKey = verificationKey;
    this.status = status;
    this.type = type;
    this.requestData = requestData;
  }

  public void startProcess() {
    this.status = BankAccountVerificationStatus.PROCESSING;
  }

  public void complete(Map<String, Object> responseData) {
    this.status = BankAccountVerificationStatus.COMPLETED;
    this.responseData = responseData;
    this.verifiedAt = LocalDateTime.now();

    // 계좌도 인증 상태로 변경
    if (this.bankAccount != null) {
      this.bankAccount.verify();
    }
  }

  public void fail(Map<String, Object> responseData) {
    this.status = BankAccountVerificationStatus.FAILED;
    this.responseData = responseData;
  }

  public void expire() {
    this.status = BankAccountVerificationStatus.EXPIRED;
    this.expiredAt = LocalDateTime.now();
  }

  public void setOneCentDepositInfo(
      BigDecimal depositPrice,
      String depositBank,
      String depositAccountNumber,
      LocalDateTime expiredAt) {
    this.depositPrice = depositPrice;
    this.depositBank = depositBank;
    this.depositAccountNumber = depositAccountNumber;
    this.expiredAt = expiredAt;
  }

  public void setBankAccount(BankAccount bankAccount) {
    this.bankAccount = bankAccount;
  }
}
