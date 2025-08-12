package liaison.groble.api.model.admin.response.maker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 페이지에서 관리자의 메모 추가에 대한 응답")
public class AdminMemoResponse {
  @Schema(
      description = "관리자 메모 내용",
      example = "불량 사용자에요",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String adminMemo;
}
