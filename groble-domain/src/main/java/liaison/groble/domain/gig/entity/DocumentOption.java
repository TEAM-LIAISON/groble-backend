package liaison.groble.domain.gig.entity;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

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
  // 컨텐츠 제공 방식
  @Enumerated(value = STRING)
  private ContentDeliveryMethod contentDeliveryMethod;
}
