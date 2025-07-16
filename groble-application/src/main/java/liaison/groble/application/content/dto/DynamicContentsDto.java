package liaison.groble.application.content.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DynamicContentsDto {
  @Builder.Default private List<DynamicContentDTO> dynamicContentDTOS = new ArrayList<>();

  public static DynamicContentsDto of(List<DynamicContentDTO> dynamicContentDTOS) {
    return DynamicContentsDto.builder().dynamicContentDTOS(dynamicContentDTOS).build();
  }
}
