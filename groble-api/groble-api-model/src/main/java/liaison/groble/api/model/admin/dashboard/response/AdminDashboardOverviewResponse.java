package liaison.groble.api.model.admin.dashboard.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 페이지에서 대시보드 전체 요약 정보에 대한 응답 DTO")
public class AdminDashboardOverviewResponse {

  @Schema(description = "총 매출 중 그로블 수수료(1.5%)")
  private BigDecimal grobleFee;

  @Schema(description = "총 매출 중 기타 금액")
  private BigDecimal etcAmount;

  @Schema(description = "총 거래액 (금액)", example = "1500000000")
  private BigDecimal totalTransactionAmount;

  @Schema(description = "총 거래 건수", example = "1234")
  private Long totalTransactionCount;

  @Schema(description = "이번 달 거래액 (금액)", example = "50000000")
  private BigDecimal monthlyTransactionAmount;

  @Schema(description = "이번 달 거래 건수", example = "89")
  private Long monthlyTransactionCount;

  @Schema(description = "회원 수", example = "5678")
  private Long userCount;

  @Schema(description = "비회원 수", example = "234")
  private Long guestUserCount;

  @Schema(description = "전체 콘텐츠 수", example = "890")
  private Long totalContentCount;

  @Schema(description = "판매 중인 콘텐츠 수", example = "567")
  private Long activeContentCount;

  @Schema(description = "자료 유형 콘텐츠 개수", example = "234")
  private Long documentTypeCount;

  @Schema(description = "서비스 유형 콘텐츠 개수", example = "123")
  private Long coachingTypeCount;

  @Schema(description = "멤버십 유형 콘텐츠 개수", example = "45")
  private Long membershipTypeCount;
}
