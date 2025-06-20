package liaison.groble.domain.user.entity;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.LazyInitializationException;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.role.Role;
import liaison.groble.domain.role.UserRole;
import liaison.groble.domain.terms.entity.Terms;
import liaison.groble.domain.terms.entity.UserTerms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.vo.IdentityVerification;
import liaison.groble.domain.user.vo.SellerInfo;
import liaison.groble.domain.user.vo.UserProfile;
import liaison.groble.domain.user.vo.UserStatusInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 사용자 엔티티 통합계정, 소셜계정 등 모든 유형의 계정에 공통으로 사용되는 사용자 정보를 관리 */
@Entity
@Table(
    name = "users",
    indexes = {@Index(name = "idx_user_nickname", columnList = "nickname")})
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@ToString(exclude = {"integratedAccount", "socialAccount", "userRoles"})
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 사용자 고유 UUID 회원 탈퇴 후 재가입 등의 상황에서도 추적 가능한 영구적인 식별자 */
  @Builder.Default
  @Column(name = "uuid", nullable = false, unique = true, updatable = false)
  private String uuid = UUID.randomUUID().toString();

  @Embedded private UserProfile userProfile;
  @Embedded private UserStatusInfo userStatusInfo;

  /** 계정 유형 INTEGRATED: 일반 로그인 계정 SOCIAL: 소셜 로그인 계정 */
  @Builder.Default
  @Column(name = "account_type", nullable = false)
  @Enumerated(STRING)
  private AccountType accountType = AccountType.INTEGRATED;

  /** 리프레시 토큰 JWT 인증에서 재발급에 사용되는 토큰 */
  @Column(name = "refresh_token", length = 500)
  private String refreshToken;

  @Column(name = "refresh_token_expires_at")
  private Instant refreshTokenExpiresAt;

  /** 마지막 로그인 시간 */
  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  /** 마지막으로 사용한 사용자 유형 (SELLER 또는 BUYER) */
  @Enumerated(STRING)
  @Column(name = "last_user_type", length = 20)
  private UserType lastUserType;

  /** 통합 계정 정보 (일반 로그인) 양방향 관계로 IntegratedAccount와 연결 */
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private IntegratedAccount integratedAccount;

  /** 소셜 계정 정보 (소셜 로그인) 양방향 관계로 SocialAccount와 연결 */
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private SocialAccount socialAccount;

  @Builder.Default
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserRole> userRoles = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private Set<UserTerms> termsAgreements = new HashSet<>();

  @Embedded private SellerInfo sellerInfo;
  @Embedded private IdentityVerification identityVerification;

  // SELLER
  @Builder.Default
  @Column(name = "is_seller")
  private boolean isSeller = false;

  /** IntegratedAccount 설정 (양방향 관계) */
  public void setIntegratedAccount(IntegratedAccount integratedAccount) {
    this.integratedAccount = integratedAccount;
  }

  /** SocialAccount 설정 (양방향 관계) */
  public void setSocialAccount(SocialAccount socialAccount) {
    this.socialAccount = socialAccount;
  }

  /** 로그인 시간 업데이트 */
  public void updateLoginTime() {
    this.lastLoginAt = Instant.now();
  }

  public void updateRefreshToken(String refreshToken, Instant refreshTokenExpiresAt) {
    this.refreshToken = refreshToken;
    this.refreshTokenExpiresAt = refreshTokenExpiresAt;
  }

  public void clearRefreshToken() {
    this.refreshToken = null;
    this.refreshTokenExpiresAt = null;
  }

  public void updateLastUserType(UserType userType) {
    this.lastUserType = userType;
  }

  // Value Object 설정 메서드
  public void setSellerInfo(SellerInfo sellerInfo) {
    this.sellerInfo = sellerInfo;
  }

  public void setIdentityVerification(IdentityVerification verification) {
    this.identityVerification = verification;
  }

  public void setSeller(boolean isSeller) {
    this.isSeller = isSeller;
  }

  // 역할 관리 메서드
  public void addRole(Role role) {
    if (this.userRoles == null) {
      this.userRoles = new HashSet<>();
    }

    UserRole userRole = new UserRole();
    userRole.setUser(this);
    userRole.setRole(role);
    this.userRoles.add(userRole);
  }

  // 유틸리티 메서드
  public String getEmail() {
    if (accountType == AccountType.INTEGRATED && integratedAccount != null) {
      return integratedAccount.getIntegratedAccountEmail();
    } else if (accountType == AccountType.SOCIAL && socialAccount != null) {
      return socialAccount.getSocialAccountEmail();
    }
    return null;
  }

  public String getPassword() {
    if (accountType == AccountType.INTEGRATED && integratedAccount != null) {
      return integratedAccount.getPassword();
    }
    return null;
  }

  // 본인인증 완료 메서드
  public void completeIdentityVerification(IdentityVerification verification) {
    this.identityVerification = verification;
  }

  /** 회원 탈퇴 처리 즉시 탈퇴 처리하고 사용자 상태를 WITHDRAWN으로 변경 */
  public void withdraw() {
    this.getUserStatusInfo().updateStatus(UserStatus.WITHDRAWN);

    // 리프레시 토큰 제거
    this.refreshToken = null;
    this.refreshTokenExpiresAt = null;
  }

  /** 사용자 정보 익명화 처리 GDPR 등 개인정보보호 규정 준수를 위한 비식별화 */
  public void anonymize() {
    // 계정 타입에 따른 이메일 익명화
    String anonymizedEmail =
        "withdrawn_"
            + this.id
            + "_"
            + UUID.randomUUID().toString().substring(0, 8)
            + "@anonymous.com";

    if (accountType == AccountType.INTEGRATED && integratedAccount != null) {
      integratedAccount.anonymizeEmail(anonymizedEmail);
    } else if (accountType == AccountType.SOCIAL && socialAccount != null) {
      socialAccount.anonymizeEmail(anonymizedEmail);
    }

    // 판매자 정보 초기화 (선택적, 법적 요구사항에 따라 보존 여부 결정)
    if (this.sellerInfo != null) {
      this.sellerInfo.anonymize();
    }

    // 본인인증 정보 초기화
    if (this.identityVerification != null) {
      this.identityVerification.anonymize();
    }
  }

  /**
   * 특정 약관 동의 여부 확인
   *
   * @param termsType 확인하려는 약관 타입
   * @return true: 한 번이라도 동의했으면 true, 아니면 false
   */
  public boolean hasAgreedTo(TermsType termsType) {
    return termsAgreements.stream()
        .filter(UserTerms::isAgreed) // 동의된 내역만
        .map(UserTerms::getTerms) // 해당 약관 엔티티
        .anyMatch(t -> t.getType() == termsType); // 타입이 일치하면 OK
  }

  /** 광고성 정보 수신 동의 여부 확인 */
  public boolean hasAgreedToAdvertising() {
    return hasAgreedTo(TermsType.MARKETING_POLICY);
  }

  /** 메이커 약관 동의 여부 확인 */
  public boolean isMakerTermsAgreed() {
    return hasAgreedTo(TermsType.SELLER_TERMS_POLICY);
  }

  /**
   * 광고성 정보 수신 동의 여부 업데이트
   *
   * @param advertisingTerms 현재 유효한 광고성 정보 약관
   * @param agreed true: 동의함, false: 미동의
   * @param ip 동의 또는 철회 시 IP
   * @param userAgent 동의 또는 철회 시 User-Agent
   */
  public void updateAdvertisingAgreement(
      Terms advertisingTerms, boolean agreed, String ip, String userAgent) {
    UserTerms existingAgreement =
        termsAgreements.stream()
            .filter(a -> a.getTerms().equals(advertisingTerms))
            .findFirst()
            .orElse(null);

    if (existingAgreement != null) {
      existingAgreement.updateAgreement(agreed, Instant.now(), ip, userAgent);
    } else {
      UserTerms newAgreement =
          UserTerms.builder()
              .user(this)
              .terms(advertisingTerms)
              .agreed(agreed)
              .agreedAt(Instant.now())
              .agreedIp(ip)
              .agreedUserAgent(userAgent)
              .build();

      termsAgreements.add(newAgreement);
    }
  }

  /**
   * 메이커 약관 동의 처리
   *
   * @param makerTerms 현재 유효한 메이커 약관
   * @param agreed 동의 여부 (true만 허용)
   * @param ip 동의 시 IP 주소
   * @param userAgent 동의 시 User-Agent
   */
  public void updateMakerTermsAgreement(
      Terms makerTerms, boolean agreed, String ip, String userAgent) {
    if (!agreed) {
      throw new IllegalArgumentException("메이커 약관은 반드시 동의해야 합니다.");
    }

    UserTerms existingAgreement =
        termsAgreements.stream()
            .filter(a -> a.getTerms().getType() == TermsType.SELLER_TERMS_POLICY)
            .findFirst()
            .orElse(null);

    if (existingAgreement != null) {
      // 기존 동의 내역 업데이트
      existingAgreement.updateAgreement(agreed, Instant.now(), ip, userAgent);
    } else {
      // 새로운 동의 내역 생성
      UserTerms newAgreement =
          UserTerms.builder()
              .user(this)
              .terms(makerTerms)
              .agreed(agreed)
              .agreedAt(Instant.now())
              .agreedIp(ip)
              .agreedUserAgent(userAgent)
              .build();

      termsAgreements.add(newAgreement);
    }
  }

  // 사업자 메이커 인증 요청 여부 확인
  public boolean isBusinessMakerVerificationRequested() {
    return sellerInfo != null
        && sellerInfo.getBusinessLicenseFileUrl() != null
        && !sellerInfo.getBusinessLicenseFileUrl().isBlank();
  }

  // 닉네임 조회
  public String getNickname() {
    return userProfile != null ? userProfile.getNickname() : null;
  }

  // 프로필 이미지 URL 조회
  public String getProfileImageUrl() {
    return userProfile != null ? userProfile.getProfileImageUrl() : null;
  }

  // 전화번호 조회
  public String getPhoneNumber() {
    return userProfile != null ? userProfile.getPhoneNumber() : null;
  }

  // 닉네임 업데이트
  public void updateNickname(String nickname) {
    if (userProfile == null) {
      userProfile = UserProfile.builder().build();
    }
    userProfile.updateNickname(nickname);
  }

  // 프로필 이미지 URL 업데이트
  public void updateProfileImageUrl(String profileImageUrl) {
    if (userProfile == null) {
      userProfile = UserProfile.builder().build();
    }
    userProfile.updateProfileImageUrl(profileImageUrl);
  }

  // 전화번호 업데이트
  public void updatePhoneNumber(String phoneNumber) {
    if (userProfile == null) {
      userProfile = UserProfile.builder().build();
    }
    userProfile.updatePhoneNumber(phoneNumber);
  }

  /** 약관 동의 여부 확인 */
  public boolean checkTermsAgreement() {
    return !this.getTermsAgreements().isEmpty();
  }

  /** 닉네임 존재 여부 확인 */
  public boolean hasNickname() {
    String nickname = getNickname();
    return nickname != null && !nickname.trim().isEmpty();
  }

  /** 약관 동의 여부 확인 (LazyInitializationException 방지) Hibernate 세션이 없어도 안전하게 확인 가능 */
  public boolean hasTermsAgreements() {
    try {
      return termsAgreements != null && !termsAgreements.isEmpty();
    } catch (LazyInitializationException e) {
      // 세션이 없는 경우 false 반환 (신규 사용자로 간주)
      return false;
    }
  }

  public boolean isWithdrawn() {
    return this.userStatusInfo.getStatus() == UserStatus.WITHDRAWN;
  }
}
