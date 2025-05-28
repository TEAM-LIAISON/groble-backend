package liaison.groble.application.content.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DynamicContentsDto {
  @Builder.Default private List<DynamicContentDto> dynamicContentDtos = new ArrayList<>();

  public static DynamicContentsDto of(List<DynamicContentDto> dynamicContentDtos) {
    return DynamicContentsDto.builder().dynamicContentDtos(dynamicContentDtos).build();
  }
}
