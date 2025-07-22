package liaison.groble.mapping.content;

import java.util.List;

import org.mapstruct.Mapper;

import liaison.groble.api.model.content.response.dynamic.DynamicContentListResponse;
import liaison.groble.api.model.content.response.dynamic.DynamicContentResponse;
import liaison.groble.application.content.dto.DynamicContentDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface DynamicContentMapper {
  DynamicContentResponse toDynamicContentResponse(DynamicContentDTO dynamicContentDTO);

  // List 매핑
  List<DynamicContentResponse> toDynamicContentResponseList(
      List<DynamicContentDTO> dynamicContentDTOs);

  // ListResponse 매핑
  default DynamicContentListResponse toDynamicContentListResponse(
      List<DynamicContentDTO> dynamicContentDTOs) {
    return DynamicContentListResponse.builder()
        .dynamicContentResponses(toDynamicContentResponseList(dynamicContentDTOs))
        .build();
  }
}
