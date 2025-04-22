package liaison.groble.domain.user.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class VerifiedEmail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  private LocalDateTime verifiedAt;

  @Builder
  private VerifiedEmail(String email, LocalDateTime verifiedAt) {
    this.email = email;
    this.verifiedAt = verifiedAt;
  }

  public static VerifiedEmail createVerifiedEmail(String email) {

    return VerifiedEmail.builder().email(email).verifiedAt(LocalDateTime.now()).build();
  }
}
