package liaison.groble.application.admin.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminGuestUserSummaryDTO {
  private final Long id;
  private final LocalDateTime createdAt;
  private final String username;
  private final String phoneNumber;
  private final String email;
  private final String phoneVerificationStatus;
  private final LocalDateTime phoneVerifiedAt;
  private final LocalDateTime verificationExpiresAt;
}
