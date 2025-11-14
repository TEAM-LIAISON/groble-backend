package liaison.groble.application.admin.dashboard.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminActiveVisitorsDTO {
  private int windowMinutes;
  private int limit;
  private LocalDateTime generatedAt;
  private List<AdminActiveMemberSessionDTO> memberSessions;
  private List<AdminActiveGuestSessionDTO> guestSessions;
  private int memberCount;
  private int guestCount;

  public List<AdminActiveMemberSessionDTO> getMemberSessions() {
    return memberSessions == null ? Collections.emptyList() : memberSessions;
  }

  public List<AdminActiveGuestSessionDTO> getGuestSessions() {
    return guestSessions == null ? Collections.emptyList() : guestSessions;
  }
}
