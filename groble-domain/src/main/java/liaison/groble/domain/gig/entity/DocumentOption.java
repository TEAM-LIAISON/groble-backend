package liaison.groble.domain.gig.entity;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;

import liaison.groble.domain.product.enums.ContentDeliveryMethod;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DOCUMENT")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class DocumentOption extends GigOption {
  @Enumerated(value = STRING)
  private ContentDeliveryMethod contentDeliveryMethod;

  // 옵션 이름
  @Column(name = "option_name")
  private String name;

  // 옵션 설명
  @Column(name = "option_description")
  private String description;

  private BigDecimal price;
}
