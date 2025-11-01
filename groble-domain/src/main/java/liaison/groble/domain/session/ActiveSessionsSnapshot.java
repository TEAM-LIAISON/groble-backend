package liaison.groble.domain.session;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActiveSessionsSnapshot {
  private final List<MemberActiveSession> memberSessions;
  private final List<GuestActiveSession> guestSessions;
  private final Instant generatedAt;

  public List<MemberActiveSession> getMemberSessions() {
    return memberSessions == null ? Collections.emptyList() : memberSessions;
  }

  public List<GuestActiveSession> getGuestSessions() {
    return guestSessions == null ? Collections.emptyList() : guestSessions;
  }
}
