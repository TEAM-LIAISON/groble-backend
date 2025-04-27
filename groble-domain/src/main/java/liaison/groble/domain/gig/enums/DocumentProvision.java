package liaison.groble.domain.gig.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentProvision {
  PROVIDED("제공"),
  NOT_PROVIDED("미제공");

  private final String description;
}
