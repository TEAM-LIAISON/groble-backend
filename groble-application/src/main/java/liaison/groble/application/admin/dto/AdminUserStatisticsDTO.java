package liaison.groble.application.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminUserStatisticsDTO {

  private final long totalUsers;
  private final long withdrawnUsers;
  private final long newUsers7Days;
  private final long newUsers30Days;

  private final long buyerOnlyCount;
  private final long buyerAndSellerCount;
  private final double buyerOnlyPercentage;
  private final double buyerAndSellerPercentage;

  private final long marketingAgreedCount;
  private final double marketingAgreedPercentage;
  private final long phoneNumberProvidedCount;
  private final double phoneNumberProvidedPercentage;
  private final long phoneNumberNotProvidedCount;
  private final double phoneNumberNotProvidedPercentage;
  private final long sellerTermsAgreedCount;
  private final double sellerTermsAgreedPercentage;

  private final VerificationStats verificationStats;
  private final double verificationSuccessRate;

  private final BusinessTypeStats businessTypeStats;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class VerificationStats {
    private final long verified;
    private final long pending;
    private final long inProgress;
    private final long failed;
    private final long none;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class BusinessTypeStats {
    private final long individualSimplified;
    private final long individualNormal;
    private final long corporate;
    private final long individualMaker;
    private final long none;
  }
}
