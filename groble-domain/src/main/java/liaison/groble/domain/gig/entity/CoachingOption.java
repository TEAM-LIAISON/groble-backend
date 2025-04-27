package liaison.groble.domain.gig.entity;

import static jakarta.persistence.EnumType.STRING;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;

import liaison.groble.domain.gig.enums.CoachingPeriod;
import liaison.groble.domain.gig.enums.CoachingType;
import liaison.groble.domain.gig.enums.DocumentProvision;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("COACHING")
@Getter
@NoArgsConstructor
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

  // Setter 메서드 추가
  public void setCoachingPeriod(CoachingPeriod coachingPeriod) {
    this.coachingPeriod = coachingPeriod;
  }

  public void setDocumentProvision(DocumentProvision documentProvision) {
    this.documentProvision = documentProvision;
  }

  public void setCoachingType(CoachingType coachingType) {
    this.coachingType = coachingType;
  }

  public void setCoachingTypeDescription(String coachingTypeDescription) {
    this.coachingTypeDescription = coachingTypeDescription;
  }
}
