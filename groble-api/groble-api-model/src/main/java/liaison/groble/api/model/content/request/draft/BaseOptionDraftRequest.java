package liaison.groble.api.model.content.request.draft;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 기본 옵션 요청 클래스 - 공통 필드 정의 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseOptionDraftRequest {

  @Size(max = 20, message = "옵션 이름은 최대 20자까지 입력할 수 있습니다.")
  @Schema(description = "옵션 이름", example = "사업계획서 컨설팅 1회")
  private String name;

  @Size(max = 60, message = "옵션 설명은 최대 60자까지 입력할 수 있습니다.")
  @Schema(description = "옵션 설명", example = "회당 30분씩 진행됩니다.")
  private String description;

  @DecimalMin(value = "0.0", inclusive = true, message = "가격은 0 이상이어야 합니다")
  @Digits(integer = 10, fraction = 0, message = "가격은 최대 10자리 정수이며, 소수점을 허용하지 않습니다.")
  @Schema(description = "가격", example = "50000")
  private BigDecimal price;
}
