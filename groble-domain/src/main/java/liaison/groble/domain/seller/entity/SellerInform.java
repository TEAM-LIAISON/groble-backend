package liaison.groble.domain.seller.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seller_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerInform {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  @Builder
  private SellerInform(User user) {
    this.user = user;
  }
}
