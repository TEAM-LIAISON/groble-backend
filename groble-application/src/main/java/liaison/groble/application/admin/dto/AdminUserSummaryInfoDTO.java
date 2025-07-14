package liaison.groble.application.admin.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserSummaryInfoDTO {
  private final LocalDateTime createdAt;

  private boolean isSellerTermsAgreed;

  private String nickname;

  private String email;

  private String phoneNumber;

  private boolean isMarketingAgreed;

  private boolean isSellerInfo;

  private String verificationStatus;

  private boolean isBusinessSeller;

  private String businessType;
}
