package liaison.groble.api.model.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserPaymentMethodResponse", description = "마이페이지 결제 수단 정보 응답")
public class UserPaymentMethodResponse {

  @Schema(description = "등록된 결제 수단 여부", example = "true")
  private boolean hasPaymentMethod;

  @Schema(description = "카드사명", example = "신한카드")
  private String cardName;

  @Schema(description = "카드 번호 뒷 4자리", example = "5678")
  private String cardNumberSuffix;

  @Schema(description = "등록된 카드로 진행 중인 구독 존재 여부", example = "false")
  private boolean hasActiveSubscription;
}
