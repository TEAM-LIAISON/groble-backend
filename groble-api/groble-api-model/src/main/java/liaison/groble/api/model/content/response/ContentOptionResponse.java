package liaison.groble.api.model.content.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 옵션 정보 응답")
public class ContentOptionResponse {
  @Schema(description = "옵션 ID", example = "1")
  private Long id;

  @Schema(description = "옵션 유형", example = "COACHING_OPTION")
  private String optionType;

  @Schema(description = "옵션 이름", example = "1시간 코칭")
  private String name;

  @Schema(description = "옵션 설명", example = "1:1 전문가 코칭 1시간")
  private String description;

  @Schema(description = "옵션 가격", example = "50000")
  private BigDecimal price;
}
