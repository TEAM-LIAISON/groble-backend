package liaison.groble.domain.user.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.*;

import liaison.groble.domain.common.utils.MapToJsonConverter;
import liaison.groble.domain.user.enums.IdentityVerificationStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IdentityVerification {

  @Column(name = "identity_verified")
  private boolean verified;

  @Column(name = "verification_method")
  @Enumerated(EnumType.STRING)
  private VerificationMethod verificationMethod;

  // 본인 인증 식별 시간
  @Column(name = "verified_at")
  private LocalDateTime verifiedAt;

  @Column(name = "verified_name")
  private String verifiedName;

  @Column(name = "verified_birth_date")
  private LocalDate verifiedBirthDate;

  @Column(name = "verified_gender")
  private String verifiedGender;

  @Column(name = "verified_phone")
  private String verifiedPhone;

  @Column(name = "verified_nationality")
  private String verifiedNationality;

  @Column(name = "ci", length = 128)
  private String ci; // 연계정보

  @Column(name = "di", length = 128)
  private String di; // 중복가입 확인정보

  @Column(name = "certification_provider")
  private String certificationProvider; // 인증기관

  @Column(name = "certification_txid")
  private String certificationTxId; // 인증 트랜잭션 ID

  // 포트원 특정 필드
  @Column(name = "port_one_request_id")
  private String portOneRequestId;

  @Column(name = "port_one_transaction_id")
  private String portOneTransactionId;

  @Column(name = "identity_status")
  @Enumerated(EnumType.STRING)
  private IdentityVerificationStatus status = IdentityVerificationStatus.NONE;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;

  @Convert(converter = MapToJsonConverter.class)
  @Column(name = "verification_data", columnDefinition = "json")
  private Map<String, Object> verificationData;

  @Builder
  public IdentityVerification(
      VerificationMethod verificationMethod,
      String verifiedName,
      LocalDate verifiedBirthDate,
      String verifiedGender,
      String verifiedPhone,
      String verifiedNationality,
      String ci,
      String di,
      String certificationProvider,
      String certificationTxId,
      String portOneRequestId,
      String portOneTransactionId,
      Map<String, Object> verificationData) {
    this.verified = true;
    this.verificationMethod = verificationMethod;
    this.verifiedAt = LocalDateTime.now();
    this.verifiedName = verifiedName;
    this.verifiedBirthDate = verifiedBirthDate;
    this.verifiedGender = verifiedGender;
    this.verifiedPhone = verifiedPhone;
    this.verifiedNationality = verifiedNationality;
    this.ci = ci;
    this.di = di;
    this.certificationProvider = certificationProvider;
    this.certificationTxId = certificationTxId;
    this.portOneRequestId = portOneRequestId;
    this.portOneTransactionId = portOneTransactionId;
    this.status = IdentityVerificationStatus.VERIFIED;
    this.verificationData = verificationData;
  }

  public void updateStatus(IdentityVerificationStatus newStatus) {
    this.status = newStatus;
    if (newStatus == IdentityVerificationStatus.VERIFIED) {
      this.verified = true;
      this.verifiedAt = LocalDateTime.now();
    } else {
      this.verified = false;
    }
  }

  public void setExpired() {
    this.status = IdentityVerificationStatus.EXPIRED;
    this.verified = false;
    this.expiredAt = LocalDateTime.now();
  }

  public enum VerificationMethod {
    PHONE("휴대폰"),
    CARD("신용카드"),
    CERTIFICATE("공동인증서"),
    INICIS("이니시스"),
    ACCOUNT_TRANSFER("계좌이체"),
    KAKAO("카카오"),
    NAVER("네이버"),
    PASS_APP("PASS 앱"),
    PAYCO("페이코");

    private final String description;

    VerificationMethod(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public void anonymize() {
    this.ci = null;
    this.di = null;
    // 필요에 따라 다른 필드 익명화
  }
}
