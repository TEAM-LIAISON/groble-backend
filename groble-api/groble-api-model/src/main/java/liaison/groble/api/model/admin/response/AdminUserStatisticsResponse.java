package liaison.groble.api.model.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 사용자 통계 응답")
public class AdminUserStatisticsResponse {

  // 전체 회원 통계
  @Schema(description = "전체 활성 회원 수", example = "15847")
  private long totalUsers;

  @Schema(description = "탈퇴한 회원 수", example = "1253")
  private long withdrawnUsers;

  @Schema(description = "최근 7일 신규 가입자 수", example = "234")
  private long newUsers7Days;

  @Schema(description = "최근 30일 신규 가입자 수", example = "1089")
  private long newUsers30Days;

  // 사용자 유형별 통계
  @Schema(description = "구매자만 활동 중인 사용자 수", example = "12678")
  private long buyerOnlyCount;

  @Schema(description = "구매자이면서 판매자인 사용자 수", example = "3169")
  private long buyerAndSellerCount;

  @Schema(description = "구매자만 비율", example = "80.0")
  private double buyerOnlyPercentage;

  @Schema(description = "구매자 & 판매자 비율", example = "20.0")
  private double buyerAndSellerPercentage;

  // 마케팅 & 동의 통계
  @Schema(description = "마케팅 수신 동의 사용자 수", example = "11386")
  private long marketingAgreedCount;

  @Schema(description = "마케팅 수신 동의율", example = "71.8")
  private double marketingAgreedPercentage;

  @Schema(description = "전화번호 입력 완료 사용자 수", example = "14530")
  private long phoneNumberProvidedCount;

  @Schema(description = "전화번호 입력 완료율", example = "91.7")
  private double phoneNumberProvidedPercentage;

  @Schema(description = "전화번호 미입력 사용자 수", example = "1317")
  private long phoneNumberNotProvidedCount;

  @Schema(description = "전화번호 미입력 비율", example = "8.3")
  private double phoneNumberNotProvidedPercentage;

  @Schema(description = "판매자 약관 동의 사용자 수", example = "3169")
  private long sellerTermsAgreedCount;

  @Schema(description = "판매자 약관 동의율", example = "20.0")
  private double sellerTermsAgreedPercentage;

  // 메이커 인증 통계
  @Schema(description = "메이커 인증 상태별 통계")
  private VerificationStats verificationStats;

  @Schema(description = "인증 성공률", example = "88.4")
  private double verificationSuccessRate;

  // 사업자 유형 통계
  @Schema(description = "사업자 유형별 통계")
  private BusinessTypeStats businessTypeStats;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class VerificationStats {
    @Schema(description = "인증 완료 건수", example = "2534")
    private long verified;

    @Schema(description = "대기 중 건수", example = "123")
    private long pending;

    @Schema(description = "진행 중 건수", example = "89")
    private long inProgress;

    @Schema(description = "실패 건수", example = "267")
    private long failed;

    @Schema(description = "미신청 건수", example = "12834")
    private long none;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BusinessTypeStats {
    @Schema(description = "간이 과세자 수", example = "1847")
    private long individualSimplified;

    @Schema(description = "일반 과세자 수", example = "956")
    private long individualNormal;

    @Schema(description = "법인 사업자 수", example = "366")
    private long corporate;

    @Schema(description = "미등록 사용자 수", example = "12678")
    private long none;
  }
}
