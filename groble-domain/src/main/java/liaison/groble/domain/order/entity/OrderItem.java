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

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.entity.Content;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "option_id", nullable = false)
  private Long optionId;

  // 옵션 관련 필드 추가
  @Column(name = "option_type")
  @Enumerated(EnumType.STRING)
  private OptionType optionType;

  // 생성자 수정
  @Builder
  public OrderItem(
      Order order,
      Content content,
      BigDecimal price,
      int quantity,
      OptionType optionType,
      Long optionId) {
    this.order = order;
    this.content = content;
    this.price = price;
    this.quantity = quantity;
    this.optionType = optionType;
    this.optionId = optionId;
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
