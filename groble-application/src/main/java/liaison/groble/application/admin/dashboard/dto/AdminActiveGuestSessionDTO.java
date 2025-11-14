package liaison.groble.application.admin.dashboard.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminActiveGuestSessionDTO {
  private final String sessionKey;
  private final Long guestId;
  private final boolean authenticated;
  private final String displayName;
  private final String email;
  private final String phoneNumber;
  private final String anonymousId;
  private final String requestUri;
  private final String httpMethod;
  private final String queryString;
  private final String referer;
  private final String clientIp;
  private final String userAgent;
  private final LocalDateTime lastSeenAt;
}
