package liaison.groble.api.model.admin.request;

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
@Schema(description = "관리자 메모 작성 요청")
public class AdminMemoRequest {
  @Schema(description = "관리자가 작성한 메모 내용", example = "악용 사용자")
  private String adminMemo;
}
