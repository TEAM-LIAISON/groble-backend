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
@Schema(description = "관리자 페이지에서 비회원 사용자 요약 정보 응답 DTO")
public class AdminGuestUserSummaryResponse {

  @Schema(description = "비회원 사용자 ID", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;

  @Schema(description = "비회원 사용자 생성 시각", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(description = "비회원 사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String username;

  @Schema(
      description = "비회원 사용자 전화번호",
      example = "010-1234-5678",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String phoneNumber;

  @Schema(
      description = "비회원 사용자 이메일",
      example = "guest@example.com",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String email;

  @Schema(
      description = "전화번호 인증 상태",
      example = "VERIFIED",
      allowableValues = {"PENDING", "VERIFIED", "EXPIRED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String phoneVerificationStatus;

  @Schema(description = "전화번호 인증 완료 시각", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime phoneVerifiedAt;

  @Schema(description = "전화번호 인증 만료 시각", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime verificationExpiresAt;
}
