package liaison.groble.domain.product.entity;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.product.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "products",
    indexes = {
      @Index(name = "idx_product_status", columnList = "product_status"),
      @Index(name = "idx_product_deleted", columnList = "deleted")
    })
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class Product extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "content_name", length = 30)
  private String contentName;

  @Column(name = "product_status", length = 20)
  @Enumerated(value = STRING)
  private ProductStatus status = ProductStatus.PENDING;

  @Column(nullable = false)
  private boolean deleted = false;

  public void updateDelete() {
    this.deleted = true;
  }
}
