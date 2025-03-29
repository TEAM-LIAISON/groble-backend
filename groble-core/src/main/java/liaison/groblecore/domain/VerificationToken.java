package liaison.groblecore.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import liaison.groblecommon.domain.base.BaseTimeEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verification_tokens")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class VerificationToken extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private LocalDateTime expiryDate;

  @Column(nullable = false)
  private boolean verified = false;

  @Builder
  public VerificationToken(String token, String email, LocalDateTime expiryDate) {
    this.token = token;
    this.email = email;
    this.expiryDate = expiryDate;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiryDate);
  }

  public void setVerified() {
    this.verified = true;
  }
}
