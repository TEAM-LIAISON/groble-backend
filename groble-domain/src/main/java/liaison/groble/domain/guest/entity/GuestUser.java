package liaison.groble.domain.guest.entity;

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
import liaison.groble.domain.guest.enums.PhoneVerificationStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "guest_users",
    indexes = {
      @Index(name = "idx_guest_phone", columnList = "phone"),
      @Index(name = "idx_guest_email", columnList = "email"),
      @Index(name = "idx_guest_verification_status", columnList = "phone_verification_status"),
      @Index(name = "idx_guest_created_at", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuestUser extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_name", nullable = true, length = 50)
  private String username;

  @Column(name = "phone_number", nullable = false, length = 20, unique = true)
  private String phoneNumber;

  @Column(name = "email", nullable = true, length = 100)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "phone_verification_status", nullable = false)
  private PhoneVerificationStatus phoneVerificationStatus = PhoneVerificationStatus.PENDING;

  @Column(name = "phone_verified_at")
  private LocalDateTime phoneVerifiedAt;

  @Column(name = "verification_expires_at")
  private LocalDateTime verificationExpiresAt;

  @Builder
  public GuestUser(String username, String phoneNumber, String email) {
    this.username = username;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.phoneVerificationStatus = PhoneVerificationStatus.PENDING;
  }

  public void verifyPhone() {
    if (this.phoneVerificationStatus == PhoneVerificationStatus.VERIFIED) {
      return;
    }

    this.phoneVerificationStatus = PhoneVerificationStatus.VERIFIED;
    this.phoneVerifiedAt = LocalDateTime.now();
    this.verificationExpiresAt = LocalDateTime.now().plusHours(1); // 1시간 유효
  }

  public void expireVerification() {
    this.phoneVerificationStatus = PhoneVerificationStatus.EXPIRED;
    this.verificationExpiresAt = null;
  }

  public boolean isVerified() {
    return this.phoneVerificationStatus == PhoneVerificationStatus.VERIFIED
        && this.verificationExpiresAt != null
        && this.verificationExpiresAt.isAfter(LocalDateTime.now());
  }

  public boolean isVerificationExpired() {
    return this.verificationExpiresAt != null
        && this.verificationExpiresAt.isBefore(LocalDateTime.now());
  }

  public void updateUserInfo(String username, String email) {
    if (username != null && !username.trim().isEmpty()) {
      this.username = username;
    }
    if (email != null && !email.trim().isEmpty()) {
      this.email = email;
    }
  }
}
