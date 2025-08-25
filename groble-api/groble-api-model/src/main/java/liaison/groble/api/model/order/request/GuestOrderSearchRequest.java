package liaison.groble.api.model.order.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "비회원 주문 조회 요청 모델")
public class GuestOrderSearchRequest {

  @Schema(
      description = "주문 번호 (merchantUid)",
      example = "20250415123456",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String merchantUid;

  @Schema(
      description = "사용자 전화번호",
      example = "010-1234-5678",
      type = "string",
      pattern = "^\\d{3}-\\d{4}-\\d{4}$",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String phoneNumber;
}
