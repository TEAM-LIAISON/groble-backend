package liaison.groble.domain.port.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeTestVerifiedInfo {
  private String phoneNumber;
  private String nickname;
  private String email;
}
