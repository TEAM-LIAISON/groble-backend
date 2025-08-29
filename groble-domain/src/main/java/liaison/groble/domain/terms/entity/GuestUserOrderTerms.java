package liaison.groble.domain.terms.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import liaison.groble.domain.guest.entity.GuestUser;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "guest_user_order_terms",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_guest_user_order_terms",
          columnNames = {"guest_user_id", "order_terms_id"})
    },
    indexes = {
      @Index(name = "idx_guest_user_order_terms_guest_user", columnList = "guest_user_id"),
      @Index(name = "idx_guest_user_order_terms_order_terms", columnList = "order_terms_id"),
      @Index(name = "idx_guest_user_order_terms_agreed_at", columnList = "agreed_at")
    })
@Getter
@NoArgsConstructor(access = PROTECTED)
public class GuestUserOrderTerms {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 게스트 사용자 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guest_user_id", nullable = false)
  private GuestUser guestUser;

  /** 동의한 약관 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_terms_id", nullable = false)
  private OrderTerms orderTerms;

  /** 동의 여부 */
  @Column(nullable = false)
  private boolean agreed;

  /** 동의한 시점 */
  @Column(name = "agreed_at", nullable = false)
  private LocalDateTime agreedAt;

  /** 동의한 IP 주소 */
  @Column(name = "agreed_ip", length = 45) // IPv6 대응
  private String agreedIp;

  /** 동의 당시 User-Agent */
  @Column(name = "agreed_user_agent", columnDefinition = "TEXT")
  private String agreedUserAgent;

  @Builder
  public GuestUserOrderTerms(
      GuestUser guestUser,
      OrderTerms orderTerms,
      boolean agreed,
      LocalDateTime agreedAt,
      String agreedIp,
      String agreedUserAgent) {
    this.guestUser = guestUser;
    this.orderTerms = orderTerms;
    this.agreed = agreed;
    this.agreedAt = agreedAt;
    this.agreedIp = agreedIp;
    this.agreedUserAgent = agreedUserAgent;
  }

  /**
   * 약관 동의 정보 업데이트
   *
   * @param agreed 동의 여부
   * @param agreedAt 동의 시점
   * @param ip 동의한 IP 주소
   * @param userAgent 동의 당시 User-Agent
   */
  public void updateAgreement(boolean agreed, LocalDateTime agreedAt, String ip, String userAgent) {
    this.agreed = agreed;
    this.agreedAt = agreedAt;
    this.agreedIp = ip;
    this.agreedUserAgent = userAgent;
  }

  /**
   * 게스트 사용자의 특정 약관 동의 여부 확인
   *
   * @return 동의 여부
   */
  public boolean isAgreed() {
    return this.agreed;
  }

  /**
   * 비회원 약관 동의 생성을 위한 팩토리 메서드
   *
   * @param guestUser 게스트 사용자
   * @param orderTerms 주문 약관
   * @param agreedIp 동의 IP
   * @param agreedUserAgent 동의 User-Agent
   * @return 생성된 비회원 약관 동의
   */
  public static GuestUserOrderTerms createAgreement(
      GuestUser guestUser, OrderTerms orderTerms, String agreedIp, String agreedUserAgent) {
    return GuestUserOrderTerms.builder()
        .guestUser(guestUser)
        .orderTerms(orderTerms)
        .agreed(true)
        .agreedAt(LocalDateTime.now())
        .agreedIp(agreedIp)
        .agreedUserAgent(agreedUserAgent)
        .build();
  }
}
