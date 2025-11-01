package liaison.groble.domain.session;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuestActiveSession {
  private final String sessionKey;
  private final Long guestId;
  private final boolean authenticated;
  private final String anonymousId;
  private final String requestUri;
  private final String httpMethod;
  private final String queryString;
  private final String referer;
  private final String clientIp;
  private final String userAgent;
  private final String clientFingerprint;
  private final Instant lastSeenAt;
}
