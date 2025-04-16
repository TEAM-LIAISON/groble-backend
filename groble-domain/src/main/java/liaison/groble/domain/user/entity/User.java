package liaison.groble.domain.user.entity;

import static liaison.groble.domain.user.enums.UserType.BUYER;
import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.seller.entity.SellerProfile;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.TermsType;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.enums.UserType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 사용자 엔티티 통합계정, 소셜계정 등 모든 유형의 계정에 공통으로 사용되는 사용자 정보를 관리 */
@Entity
@Table(
    name = "users",
    indexes = {@Index(name = "idx_user_nick_name", columnList = "nick_name")})
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

  /** 사용자 이름 (닉네임) */
  @Column(name = "nick_name", length = 50)
  private String nickName;

  /** 사용자 프로필 이미지 URL */
  @Column(name = "profile_image_url", columnDefinition = "TEXT")
  private String profileImageUrl;

  /** 사용자 전화번호 */
  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  /** 통합 계정 정보 (일반 로그인) 양방향 관계로 IntegratedAccount와 연결 */
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private IntegratedAccount integratedAccount;

  /** 소셜 계정 정보 (소셜 로그인) 양방향 관계로 SocialAccount와 연결 */
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private SocialAccount socialAccount;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private SellerProfile sellerProfile;

  /** 마지막 로그인 시간 */
  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @Builder.Default // 이 애노테이션 추가
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserRole> userRoles = new HashSet<>();

  /** 리프레시 토큰 JWT 인증에서 재발급에 사용되는 토큰 */
  @Column(name = "refresh_token", length = 500)
  private String refreshToken;

  /** 계정 유형 INTEGRATED: 일반 로그인 계정 SOCIAL: 소셜 로그인 계정 */
  @Builder.Default
  @Column(name = "account_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private AccountType accountType = AccountType.INTEGRATED;

  /**
   * 사용자 상태 ACTIVE: 활성 상태 INACTIVE: 비활성 상태 DORMANT: 휴면 상태 LOCKED: 잠금 상태 SUSPENDED: 정지 상태
   * PENDING_VERIFICATION: 이메일 인증 대기 PENDING_WITHDRAWAL: 탈퇴 요청 상태 WITHDRAWN: 탈퇴 완료 상태
   */
  @Builder.Default
  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserStatus status = UserStatus.PENDING_VERIFICATION;

  /** 상태 변경 시간 상태가 변경된 마지막 시간 (휴면 계정 전환, 계정 활성화 등을 추적) */
  @Column(name = "status_changed_at")
  private Instant statusChangedAt;

  /** 마케팅 수신 동의 여부 */
  @Builder.Default
  @Column(name = "marketing_consent", nullable = false)
  private boolean marketingConsent = false;

  /** 마케팅 수신 동의 시간 */
  @Column(name = "marketing_consent_at")
  private Instant marketingConsentAt;

  /** 마지막으로 사용한 사용자 유형 (SELLER 또는 BUYER) */
  @Column(name = "last_user_type", length = 20)
  private UserType lastUserType;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private Set<UserTermsAgreement> termsAgreements = new HashSet<>();

  /**
   * 통합 계정으로부터 유저 생성 메서드 IntegratedAccount를 먼저 생성하고 그로부터 User를 생성
   *
   * @param integratedAccount 통합 계정 정보
   * @return 생성된 User 객체
   */
  public static User fromIntegratedAccount(IntegratedAccount integratedAccount) {
    User user =
        User.builder()
            .accountType(AccountType.INTEGRATED)
            .status(UserStatus.PENDING_VERIFICATION) // 이메일 인증 대기 상태로 설정
            .statusChangedAt(Instant.now())
            .lastUserType(BUYER) // 기본값으로 BUYER 설정
            .build();

    user.setIntegratedAccount(integratedAccount);
    return user;
  }

  /**
   * 소셜 계정으로부터 유저 생성 메서드 SocialAccount를 먼저 생성하고 그로부터 User를 생성
   *
   * @param socialAccount 소셜 계정 정보
   * @return 생성된 User 객체
   */
  public static User fromSocialAccount(SocialAccount socialAccount) {
    User user =
        User.builder()
            .accountType(AccountType.SOCIAL)
            .status(UserStatus.ACTIVE) // 소셜 로그인은 즉시 활성화
            .statusChangedAt(Instant.now())
            .lastUserType(BUYER) // 기본값으로 BUYER 설정
            .build();

    user.setSocialAccount(socialAccount);
    return user;
  }

  /** IntegratedAccount 설정 (양방향 관계) */
  public void setIntegratedAccount(IntegratedAccount integratedAccount) {
    this.integratedAccount = integratedAccount;
  }

  /** SocialAccount 설정 (양방향 관계) */
  public void setSocialAccount(SocialAccount socialAccount) {
    this.socialAccount = socialAccount;
  }

  // User.java - addRole 메서드만 변경
  public void addRole(Role role) {
    // null 체크와 초기화 추가
    if (this.userRoles == null) {
      this.userRoles = new HashSet<>();
    }

    UserRole userRole = new UserRole();
    userRole.setUser(this);
    userRole.setRole(role);
    this.userRoles.add(userRole);
  }

  /** 로그인 시간 업데이트 */
  public void updateLoginTime() {
    this.lastLoginAt = Instant.now();
  }

  /**
   * 리프레시 토큰 업데이트
   *
   * @param refreshToken 새 리프레시 토큰
   */
  public void updateRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  /**
   * 마지막 사용자 유형 업데이트
   *
   * @param userType 사용자 유형 (SELLER 또는 BUYER)
   */
  public void updateLastUserType(UserType userType) {
    this.lastUserType = userType;
  }

  /**
   * 이메일 조회 메서드 계정 타입에 따라 적절한 이메일 반환
   *
   * @return 사용자 이메일
   */
  public String getEmail() {
    if (accountType == AccountType.INTEGRATED && integratedAccount != null) {
      return integratedAccount.getIntegratedAccountEmail();
    } else if (accountType == AccountType.SOCIAL && socialAccount != null) {
      return socialAccount.getSocialAccountEmail();
    }

    return null;
  }

  /**
   * 사용자 비밀번호 메서드
   *
   * @return 사용자 비밀번호
   */
  public String getPassword() {
    if (accountType == AccountType.INTEGRATED && integratedAccount != null) {
      return integratedAccount.getPassword();
    }

    return null;
  }

  /**
   * 사용자 상태 업데이트
   *
   * @param newStatus 새로운 상태
   */
  public void updateStatus(UserStatus newStatus) {
    this.status = newStatus;
    this.statusChangedAt = Instant.now();
  }

  /** 이메일 인증 완료 처리 인증 대기 상태인 경우만 활성화 */
  public void verifyEmail() {
    if (this.status == UserStatus.PENDING_VERIFICATION) {
      this.updateStatus(UserStatus.ACTIVE);
    }
  }

  /** 사용자 계정 잠금 관리자에 의한 계정 잠금 처리 */
  public void lock() {
    this.updateStatus(UserStatus.LOCKED);
  }

  /** 사용자 계정 일시 정지 일정 기간 동안 계정 사용 제한 */
  public void suspend() {
    this.updateStatus(UserStatus.SUSPENDED);
  }

  /** 사용자 탈퇴 요청 즉시 탈퇴하지 않고 유예 기간을 둠 */
  public void requestWithdrawal() {
    this.updateStatus(UserStatus.PENDING_WITHDRAWAL);
  }

  /** 사용자 탈퇴 확정 개인정보는 비식별화 처리 */
  public void confirmWithdrawal() {
    this.updateStatus(UserStatus.WITHDRAWN);

    // 개인정보 비식별화 처리
    String anonymizedEmail = "withdrawn_" + this.id + "@example.com";

    // 계정 타입에 따라 적절한 이메일 필드 업데이트
    if (accountType == AccountType.INTEGRATED && integratedAccount != null) {
      // 별도 메서드를 통해 이메일 업데이트 (IntegratedAccount 클래스에 추가 필요)
      // integratedAccount.updateEmail(anonymizedEmail);
    } else if (accountType == AccountType.SOCIAL && socialAccount != null) {
      // 별도 메서드를 통해 이메일 업데이트 (SocialAccount 클래스에 추가 필요)
      // socialAccount.updateEmail(anonymizedEmail);
    }

    this.nickName = "탈퇴한 사용자";
    this.refreshToken = null;
  }

  /** 계정 활성화 */
  public void activate() {
    this.updateStatus(UserStatus.ACTIVE);
  }

  /** 계정 비활성화 */
  public void deactivate() {
    this.updateStatus(UserStatus.INACTIVE);
  }

  /**
   * 계정 접근 가능 여부 확인
   *
   * @return 접근 가능 여부
   */
  public boolean isAccessible() {
    return this.status.isAccessible();
  }

  /**
   * 로그인 가능 여부 확인
   *
   * @return 로그인 가능 여부
   */
  public boolean isLoginable() {
    return this.status.isLoginable();
  }

  /**
   * 마케팅 수신 동의 설정
   *
   * @param consent 동의 여부
   */
  public void setMarketingConsent(boolean consent) {
    this.marketingConsent = consent;
    if (consent) {
      this.marketingConsentAt = Instant.now();
    }
  }

  /**
   * 사용자 이름 업데이트
   *
   * @param nickName 새 닉네임
   */
  public void updateNickName(String nickName) {
    this.nickName = nickName;
  }

  public void agreeToTerms(Terms terms, String agreedIp, String agreedUserAgent) {
    UserTermsAgreement agreement =
        UserTermsAgreement.builder()
            .user(this)
            .terms(terms)
            .agreed(true)
            .agreedIp(agreedIp)
            .agreedUserAgent(agreedUserAgent)
            .build();

    termsAgreements.add(agreement);
  }

  public boolean hasAgreedTo(TermsType termsType) {
    return termsAgreements.stream()
        .anyMatch(
            agreement ->
                agreement.getTerms().getType() == termsType
                    && agreement.isAgreed()
                    && agreement.getTerms().getEffectiveTo() == null);
  }

  public boolean hasAgreedToAllRequiredTerms() {
    return Arrays.stream(TermsType.values())
        .filter(TermsType::isRequired)
        .allMatch(this::hasAgreedTo);
  }

  public List<TermsType> getMissingRequiredTerms() {
    return Arrays.stream(TermsType.values())
        .filter(TermsType::isRequired)
        .filter(type -> !hasAgreedTo(type))
        .collect(Collectors.toList());
  }
}
