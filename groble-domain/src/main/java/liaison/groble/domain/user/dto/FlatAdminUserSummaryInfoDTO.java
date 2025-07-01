package liaison.groble.domain.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlatAdminUserSummaryInfoDTO {
  private LocalDateTime createdAt;

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
