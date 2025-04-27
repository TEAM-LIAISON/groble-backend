package liaison.groble.domain.gig.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoachingPeriod {
  ONE_DAY("1일"),
  TWO_TO_SIX_DAYS("2~6일"),
  MORE_THAN_ONE_WEEK("일주일 이상");

  private final String description;
}
