package liaison.groble.domain.order.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.gig.entity.Gig;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gig_id", nullable = false)
  private Gig gig;

  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "content_name", nullable = false)
  private String contentName;

  // 옵션 관련 필드 추가
  @Column(name = "option_type")
  @Enumerated(EnumType.STRING)
  private OptionType optionType;

  @Column(name = "option_id")
  private Long optionId;

  @Column(name = "option_name")
  private String optionName;

  // 생성자 수정
  @Builder
  public OrderItem(
      Order order,
      Gig gig,
      BigDecimal price,
      int quantity,
      OptionType optionType,
      Long optionId,
      String optionName) {
    this.order = order;
    this.gig = gig;
    this.price = price;
    this.quantity = quantity;
    this.contentName = gig.getTitle();
    this.optionType = optionType;
    this.optionId = optionId;
    this.optionName = optionName;
  }

  // 비즈니스 메서드
  public BigDecimal getTotalPrice() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }

  // 옵션 타입 enum 추가
  public enum OptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
