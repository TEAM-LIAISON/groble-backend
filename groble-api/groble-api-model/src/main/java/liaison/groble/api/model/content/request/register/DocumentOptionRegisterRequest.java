package liaison.groble.api.model.content.request.register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import liaison.groble.api.model.content.request.draft.BaseOptionDraftRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 문서 옵션 요청 클래스 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOptionRegisterRequest extends BaseOptionDraftRequest {
  @NotBlank(message = "컨텐츠 제공 방식은 필수 입력 항목입니다")
  @Pattern(regexp = "^(IMMEDIATE_DOWNLOAD|FUTURE_UPLOAD)$", message = "유효한 컨텐츠 제공 방식이 아닙니다")
  @Schema(description = "컨텐츠 제공 방식", example = "IMMEDIATE_DOWNLOAD")
  private String contentDeliveryMethod;
}
