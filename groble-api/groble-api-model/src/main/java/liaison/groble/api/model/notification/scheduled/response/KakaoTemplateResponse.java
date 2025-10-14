package liaison.groble.api.model.notification.scheduled.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카카오 알림톡 템플릿 응답")
public class KakaoTemplateResponse {

  @Schema(description = "템플릿 키", example = "purchase-complete")
  private String key;

  @Schema(description = "템플릿 코드", example = "bizp_2025082019070532533937376")
  private String code;

  @Schema(description = "템플릿 명", example = "구매 완료 알림")
  private String name;
}
