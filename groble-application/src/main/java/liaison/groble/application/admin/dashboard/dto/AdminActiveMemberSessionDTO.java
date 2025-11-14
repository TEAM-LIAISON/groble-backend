package liaison.groble.application.admin.dashboard.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminActiveMemberSessionDTO {
  private final String sessionKey;
  private final Long userId;
  private final String nickname;
  private final String email;
  private final String phoneNumber;
  private final String accountType;
  private final String lastUserType;
  private final List<String> roles;
  private final String requestUri;
  private final String httpMethod;
  private final String queryString;
  private final String referer;
  private final String clientIp;
  private final String userAgent;
  private final LocalDateTime lastSeenAt;

  public List<String> getRoles() {
    return roles == null ? Collections.emptyList() : roles;
  }
}
