package liaison.groble.application.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;

import liaison.groble.domain.session.ActiveSessionStore;
import liaison.groble.domain.session.ActiveSessionsSnapshot;
import liaison.groble.domain.session.GuestActiveSession;
import liaison.groble.domain.session.MemberActiveSession;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActiveSessionService {

  private static final Duration SESSION_TTL = Duration.ofMinutes(10);

  private final ActiveSessionStore activeSessionStore;

  public void recordMemberActivity(MemberActivityCommand command) {
    if (command == null || command.getUserId() == null || command.getSessionKey() == null) {
      return;
    }

    Instant lastSeen = Objects.requireNonNullElse(command.getOccurredAt(), Instant.now());

    MemberActiveSession session =
        MemberActiveSession.builder()
            .sessionKey(command.getSessionKey())
            .userId(command.getUserId())
            .accountType(command.getAccountType())
            .lastUserType(command.getLastUserType())
            .roles(command.getRoles())
            .requestUri(command.getRequestUri())
            .httpMethod(command.getHttpMethod())
            .queryString(command.getQueryString())
            .referer(command.getReferer())
            .clientIp(command.getClientIp())
            .userAgent(command.getUserAgent())
            .clientFingerprint(command.getClientFingerprint())
            .lastSeenAt(lastSeen)
            .build();

    activeSessionStore.upsertMemberSession(session, SESSION_TTL);
  }

  public void recordGuestActivity(GuestActivityCommand command) {
    if (command == null || command.getSessionKey() == null) {
      return;
    }

    Instant lastSeen = Objects.requireNonNullElse(command.getOccurredAt(), Instant.now());

    GuestActiveSession session =
        GuestActiveSession.builder()
            .sessionKey(command.getSessionKey())
            .guestId(command.getGuestId())
            .authenticated(command.isAuthenticated())
            .anonymousId(command.getAnonymousId())
            .requestUri(command.getRequestUri())
            .httpMethod(command.getHttpMethod())
            .queryString(command.getQueryString())
            .referer(command.getReferer())
            .clientIp(command.getClientIp())
            .userAgent(command.getUserAgent())
            .clientFingerprint(command.getClientFingerprint())
            .lastSeenAt(lastSeen)
            .build();

    activeSessionStore.upsertGuestSession(session, SESSION_TTL);
  }

  public ActiveSessionsSnapshot getActiveSessions(Duration window, int limit) {
    Duration effectiveWindow =
        (window == null || window.isZero() || window.isNegative()) ? Duration.ofMinutes(5) : window;
    int effectiveLimit = limit <= 0 ? 50 : Math.min(limit, 200);

    Instant threshold = Instant.now().minus(effectiveWindow);

    return ActiveSessionsSnapshot.builder()
        .memberSessions(activeSessionStore.findActiveMemberSessions(threshold, effectiveLimit))
        .guestSessions(activeSessionStore.findActiveGuestSessions(threshold, effectiveLimit))
        .generatedAt(Instant.now())
        .build();
  }
}
