package liaison.groble.api.model.content.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "optionType",
    visible = true)
@JsonSubTypes({@JsonSubTypes.Type(name = "DOCUMENT_OPTION", value = DocumentOptionResponse.class)})
@Schema(
    name = "BaseOptionResponse",
    description = "콘텐츠 옵션 공통 정보 응답",
    discriminatorProperty = "optionType")
public abstract class BaseOptionResponse {
  @Schema(description = "옵션 ID", example = "1")
  private Long optionId;

  @Schema(description = "옵션 이름", example = "기본 옵션")
  private String name;

  @Schema(description = "옵션 설명", example = "기본적인 설명입니다.")
  private String description;

  @Schema(description = "옵션 가격", example = "50000")
  private BigDecimal price;

  @Schema(description = "옵션 유형", example = "COACHING_OPTION", required = true)
  public abstract String getOptionType();
}
