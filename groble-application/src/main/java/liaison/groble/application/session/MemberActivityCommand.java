package liaison.groble.application.session;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberActivityCommand {
  private final String sessionKey;
  private final Long userId;
  private final String accountType;
  private final String lastUserType;
  private final List<String> roles;
  private final String requestUri;
  private final String httpMethod;
  private final String queryString;
  private final String referer;
  private final String clientIp;
  private final String userAgent;
  private final String clientFingerprint;
  private final Instant occurredAt;
}
