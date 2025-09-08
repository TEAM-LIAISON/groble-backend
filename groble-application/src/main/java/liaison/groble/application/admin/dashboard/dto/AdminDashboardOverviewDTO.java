package liaison.groble.application.admin.dashboard.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardOverviewDTO {
  private BigDecimal grobleFee;
  private BigDecimal etcAmount;
  private BigDecimal totalTransactionAmount;
  private Long totalTransactionCount;
  private BigDecimal monthlyTransactionAmount;
  private Long monthlyTransactionCount;
  private Long userCount;
  private Long guestUserCount;
  private Long totalContentCount;
  private Long activeContentCount;
  private Long documentTypeCount;
  private Long coachingTypeCount;
  private Long membershipTypeCount;
}
