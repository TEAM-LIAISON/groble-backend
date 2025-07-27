package liaison.groble.domain.user.vo;

import static jakarta.persistence.EnumType.STRING;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;

import liaison.groble.domain.user.enums.UserStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserStatusInfo {
  @Enumerated(STRING)
  @Column(name = "status", nullable = false)
  private UserStatus status;

  @Column(name = "status_changed_at", nullable = false)
  private LocalDateTime statusChangedAt;

  public void updateStatus(UserStatus newStatus) {
    this.status = newStatus;
    this.statusChangedAt = LocalDateTime.now();
  }

  public boolean isLoginable() {
    return status.isLoginable();
  }
}
