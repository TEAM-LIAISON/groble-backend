package liaison.groble.api.model.admin.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 페이지에서 사용자 요약 정보 응답 Response DTO")
public class AdminUserSummaryInfoResponse {

  @Schema(description = "사용자 회원가입 시간", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(
      description = "메이커 이용 약관 동의 여부 (true: 메이커 및 구매자, false: 구매자)",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean isSellerTermsAgreed;

  @Schema(
      description = "사용자 닉네임",
      example = "홍길동",
      type = "string",
      maxLength = 50,
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String nickname;

  @Schema(
      description = "사용자 이메일 주소",
      example = "user@example.com",
      type = "string",
      format = "email",
      maxLength = 100,
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Schema(
      description = "사용자 전화번호",
      example = "010-1234-5678",
      type = "string",
      pattern = "^\\d{3}-\\d{4}-\\d{4}$",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String phoneNumber;

  @Schema(
      description = "사용자 상태",
      example = "ACTIVE",
      allowableValues = {
        "ACTIVE",
        "INACTIVE",
        "DORMANT",
        "LOCKED",
        "SUSPENDED",
        "PENDING_WITHDRAWAL",
        "WITHDRAWN",
        "PENDING_VERIFICATION"
      },
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String userStatus;

  @Schema(
      description = "마케팅 수신 동의 여부",
      example = "false",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean isMarketingAgreed;

  @Schema(
      description = "판매자 정보 등록 여부 (false: 메이커 인증 부분에서 '해당없음'으로 띄워주기, true: 메이커 인증 부분에서 상태를 나타내야함)",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean isSellerInfo;

  @Schema(
      description = "메이커 인증 상태 (isSellerInfo가 true라는 전제 하에 사용됩니다.)",
      example = "VERIFIED",
      type = "string",
      allowableValues = {"PENDING", "IN_PROGRESS", "FAILED", "VERIFIED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String verificationStatus;

  @Schema(
      description = "사업자 판매자 여부 (개인 판매자: false, 사업자 판매자: true)",
      example = "false",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean isBusinessSeller;

  /** 5. 사업자 유형 */
  @Schema(
      description = "사업자 유형 (예: 개인·간이, 개인·일반, 법인)",
      example = "individual-simple",
      type = "string",
      allowableValues = {"individual-simple", "individual-general", "corporation"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String businessType;

  @Schema(
      description = "사용자 탈퇴 사유 (탈퇴 사용자인 경우)",
      example = "서비스를 잘 이용하지 않아요",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String withdrawalReason;
}
