package liaison.groble.domain.session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface ActiveSessionStore {

  void upsertMemberSession(MemberActiveSession session, Duration ttl);

  void upsertGuestSession(GuestActiveSession session, Duration ttl);

  List<MemberActiveSession> findActiveMemberSessions(Instant threshold, int limit);

  List<GuestActiveSession> findActiveGuestSessions(Instant threshold, int limit);
}
