package liaison.groble.domain.user.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;

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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_terms",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_terms",
          columnNames = {"user_id", "terms_id"})
    })
@Getter
@NoArgsConstructor(access = PROTECTED)
public class UserTerms {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 사용자 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** 동의한 약관 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "terms_id", nullable = false)
  private Terms terms;

  /** 동의 여부 */
  @Column(nullable = false)
  private boolean agreed;

  /** 동의한 시점 */
  @Column(nullable = false)
  private Instant agreedAt;

  /** 동의한 IP 주소 */
  @Column(length = 45) // IPv6 대응
  private String agreedIp;

  /** 동의 당시 User-Agent */
  @Column(columnDefinition = "TEXT")
  private String agreedUserAgent;

  @Builder
  public UserTerms(
      User user,
      Terms terms,
      boolean agreed,
      Instant agreedAt,
      String agreedIp,
      String agreedUserAgent) {
    this.user = user;
    this.terms = terms;
    this.agreed = agreed;
    this.agreedAt = agreedAt;
    this.agreedIp = agreedIp;
    this.agreedUserAgent = agreedUserAgent;
  }

  public void updateAgreement(boolean agreed, Instant agreedAt, String ip, String userAgent) {
    this.agreed = agreed;
    this.agreedAt = agreedAt;
    this.agreedIp = ip;
    this.agreedUserAgent = userAgent;
  }
}
