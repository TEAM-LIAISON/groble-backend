package liaison.groble.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemDetails {
  private String nickname;
  private String systemTitle;

  // 그로블 환영 알림
  public static SystemDetails welcomeGroble(final String nickname, final String systemTitle) {
    return SystemDetails.builder().nickname(nickname).systemTitle(systemTitle).build();
  }
}
