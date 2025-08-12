package liaison.groble.application.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

/** 대시보드 통계 DTO 총 수익, 결제 건수, N월 수익, N월 결제 건수, 고객수 (전체 및 신규) 제공 */
@Getter
@Builder
public class DashboardStatsDTO {
  // ===== 전체 통계 =====
  private BigDecimal totalRevenue; // 총 수익
  private Integer totalPaymentCount; // 총 결제 건수
  private Integer totalCustomerCount; // 전체 고객 수

  // ===== 이번 달 통계 =====
  private BigDecimal monthRevenue; // 이번 달 수익
  private Integer monthPaymentCount; // 이번 달 결제 건수
  private Integer monthNewCustomerCount; // 이번 달 신규 고객 수
  private BigDecimal monthRevenueGrowthRate; // 전월 대비 매출 성장률

  // ===== 오늘 통계 (실시간) =====
  private TodayStats todayStats;

  // ===== 추가 지표 =====

  /** 오늘 통계 (실시간) */
  @Getter
  @Builder
  public static class TodayStats {
    private BigDecimal revenue; // 오늘 수익
    private Integer paymentCount; // 오늘 결제 건수
    private Integer newCustomerCount; // 오늘 신규 고객 수
    private Integer uniqueVisitors; // 방문자 수 (옵션)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
  }
}
