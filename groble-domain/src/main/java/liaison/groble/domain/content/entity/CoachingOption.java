package liaison.groble.domain.content.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("COACHING")
@Getter
@NoArgsConstructor
public class CoachingOption extends ContentOption {
  // 별도 필드 없음 – 공통 필드만 상속
}
