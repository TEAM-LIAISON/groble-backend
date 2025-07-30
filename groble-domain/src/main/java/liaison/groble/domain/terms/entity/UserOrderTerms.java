package liaison.groble.domain.terms.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import liaison.groble.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_order_terms",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_order_terms",
          columnNames = {"user_id", "order_terms_id"})
    })
@Getter
@NoArgsConstructor(access = PROTECTED)
public class UserOrderTerms {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 사용자 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** 동의한 약관 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_terms_id", nullable = false)
  private OrderTerms orderTerms;

  /** 동의 여부 */
  @Column(nullable = false)
  private boolean agreed;

  /** 동의한 시점 */
  @Column(nullable = false)
  private LocalDateTime agreedAt;

  /** 동의한 IP 주소 */
  @Column(length = 45) // IPv6 대응
  private String agreedIp;

  /** 동의 당시 User-Agent */
  @Column(columnDefinition = "TEXT")
  private String agreedUserAgent;

  @Builder
  public UserOrderTerms(
      User user,
      OrderTerms orderTerms,
      boolean agreed,
      LocalDateTime agreedAt,
      String agreedIp,
      String agreedUserAgent) {
    this.user = user;
    this.orderTerms = orderTerms;
    this.agreed = agreed;
    this.agreedAt = agreedAt;
    this.agreedIp = agreedIp;
    this.agreedUserAgent = agreedUserAgent;
  }

  public void updateAgreement(boolean agreed, LocalDateTime agreedAt, String ip, String userAgent) {
    this.agreed = agreed;
    this.agreedAt = agreedAt;
    this.agreedIp = ip;
    this.agreedUserAgent = userAgent;
  }
}
