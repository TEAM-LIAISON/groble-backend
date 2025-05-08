package liaison.groble.api.model.content.request.examine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReviewRequest {
  @Schema(description = "심사 액션 (APPROVE: 승인, REJECT: 반려)", example = "APPROVE")
  private String action;

  // 필요한 경우 반려 사유 등 추가 필드를 여기에 정의할 수 있습니다
  @Schema(description = "반려 사유 (반려 시에만 필요)", example = "규정에 맞지 않는 콘텐츠입니다")
  private String rejectReason;
}
