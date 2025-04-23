package liaison.groble.domain.purchase.entity;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import liaison.groble.domain.product.entity.Product;
import liaison.groble.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Purchase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "product_id", unique = true, nullable = false)
  private Product product;
}
