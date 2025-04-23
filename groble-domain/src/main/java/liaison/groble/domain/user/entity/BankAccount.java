package liaison.groble.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.user.enums.BankAccountStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bank_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BankAccount extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private String bankCode;

  @Column(nullable = false)
  private String bankName;

  @Column(nullable = false)
  private String accountNumber;

  @Column(nullable = false)
  private String accountHolderName;

  @Column private String accountHolderId; // 주민번호 앞자리 또는 사업자번호

  @Column private String accountHolderType; // 개인 or 법인

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BankAccountStatus status;

  @Column private LocalDateTime verifiedAt;

  @OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BankAccountVerification> verifications = new ArrayList<>();

  @Column private boolean isPrimary; // 대표 계좌 여부

  @Builder
  public BankAccount(
      User user,
      String bankCode,
      String bankName,
      String accountNumber,
      String accountHolderName,
      String accountHolderId,
      String accountHolderType) {
    this.user = user;
    this.bankCode = bankCode;
    this.bankName = bankName;
    this.accountNumber = accountNumber;
    this.accountHolderName = accountHolderName;
    this.accountHolderId = accountHolderId;
    this.accountHolderType = accountHolderType;
    this.status = BankAccountStatus.REGISTERED;
  }

  public void verify() {
    this.status = BankAccountStatus.VERIFIED;
    this.verifiedAt = LocalDateTime.now();
  }

  public void markAsPrimary() {
    this.isPrimary = true;
  }

  public void markAsInvalid() {
    this.status = BankAccountStatus.INVALID;
  }

  public void delete() {
    this.status = BankAccountStatus.DELETED;
  }

  public void addVerification(BankAccountVerification verification) {
    this.verifications.add(verification);
    verification.setBankAccount(this);
  }
}
