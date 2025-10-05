package liaison.groble.application.admin.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminHomeTestContactDTO {
  private final Long id;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;
  private final String phoneNumber;
  private final String email;
  private final String nickname;
}
