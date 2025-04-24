package liaison.groble.domain.gig.entity;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;

import liaison.groble.domain.product.enums.CoachingPeriod;
import liaison.groble.domain.product.enums.CoachingType;
import liaison.groble.domain.product.enums.DocumentProvision;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("COACHING")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class CoachingOption extends GigOption {
  // 코칭 기간
  @Enumerated(value = STRING)
  private CoachingPeriod coachingPeriod;

  // 자료 제공
  @Enumerated(value = STRING)
  private DocumentProvision documentProvision;

  // 코칭 방식
  @Enumerated(value = STRING)
  private CoachingType coachingType;

  // 코칭 방식 설명
  private String coachingTypeDescription;
}
