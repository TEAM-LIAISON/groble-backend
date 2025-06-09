package liaison.groble.api.model.content.request.examine;

import jakarta.validation.constraints.NotNull;

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
public class ContentExamineRequest {

  @Schema(description = "심사 액션", example = "APPROVE")
  @NotNull
  private ExamineAction action;

  @Schema(description = "반려 사유 (반려 시에만 필요)", example = "규정에 맞지 않는 콘텐츠입니다")
  private String rejectReason;

  public enum ExamineAction {
    @Schema(description = "승인")
    APPROVE,
    @Schema(description = "반려")
    REJECT
  }
}
