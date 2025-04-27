package liaison.groble.domain.gig.entity;

import static jakarta.persistence.EnumType.STRING;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;

import liaison.groble.domain.gig.enums.ContentDeliveryMethod;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DOCUMENT")
@Getter
@NoArgsConstructor
public class DocumentOption extends GigOption {
  // 컨텐츠 제공 방식
  @Enumerated(value = STRING)
  private ContentDeliveryMethod contentDeliveryMethod;

  // Setter 메서드 추가
  public void setContentDeliveryMethod(ContentDeliveryMethod contentDeliveryMethod) {
    this.contentDeliveryMethod = contentDeliveryMethod;
  }
}
