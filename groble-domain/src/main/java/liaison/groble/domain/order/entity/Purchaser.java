package liaison.groble.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchaser {

  @Column(name = "purchaser_name", nullable = false)
  private String name;

  @Column(name = "purchaser_email", nullable = false)
  private String email;

  @Column(name = "purchaser_phone")
  private String phone;

  @Builder
  public Purchaser(String name, String email, String phone) {
    this.name = name;
    this.email = email;
    this.phone = phone;
  }
}
