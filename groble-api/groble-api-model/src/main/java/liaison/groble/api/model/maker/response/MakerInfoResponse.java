package liaison.groble.api.model.maker.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메이커 기본 정보 응답")
public class MakerInfoResponse {

  @Schema(
      description = "메이커 유형",
      example = "PERSONAL",
      allowableValues = {"UNVERIFIED", "PERSONAL", "BUSINESS"})
  private String makerType;

  @Schema(description = "메이커 인증 여부", example = "true")
  private boolean verified;

  @Schema(
      description = "메이커 인증 상태 코드",
      example = "VERIFIED",
      allowableValues = {"PENDING", "IN_PROGRESS", "FAILED", "VERIFIED"})
  private String verificationStatus;

  @Schema(description = "메이커 인증 상태 라벨", example = "인증 완료 ✅")
  private String verificationStatusLabel;

  @Schema(description = "메이커 이름", example = "홍길동")
  private String name;

  @Schema(description = "메이커 이메일", example = "maker@example.com")
  private String email;

  @Schema(description = "메이커 전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @Schema(description = "사업자 메이커 정보", implementation = BusinessMakerInfoResponse.class)
  private BusinessMakerInfoResponse businessInfo;
}
