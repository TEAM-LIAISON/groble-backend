package liaison.groble.mapping.content;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.response.dynamic.DynamicContentResponse;
import liaison.groble.application.content.dto.DynamicContentDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-30T17:38:21+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class DynamicContentMapperImpl implements DynamicContentMapper {

  @Override
  public DynamicContentResponse toDynamicContentResponse(DynamicContentDTO dynamicContentDTO) {
    if (dynamicContentDTO == null) {
      return null;
    }

    DynamicContentResponse.DynamicContentResponseBuilder dynamicContentResponse =
        DynamicContentResponse.builder();

    if (dynamicContentDTO.getContentId() != null) {
      dynamicContentResponse.contentId(dynamicContentDTO.getContentId());
    }
    if (dynamicContentDTO.getTitle() != null) {
      dynamicContentResponse.title(dynamicContentDTO.getTitle());
    }
    if (dynamicContentDTO.getContentType() != null) {
      dynamicContentResponse.contentType(dynamicContentDTO.getContentType());
    }
    if (dynamicContentDTO.getThumbnailUrl() != null) {
      dynamicContentResponse.thumbnailUrl(dynamicContentDTO.getThumbnailUrl());
    }
    if (dynamicContentDTO.getUpdatedAt() != null) {
      dynamicContentResponse.updatedAt(dynamicContentDTO.getUpdatedAt());
    }

    return dynamicContentResponse.build();
  }

  @Override
  public List<DynamicContentResponse> toDynamicContentResponseList(
      List<DynamicContentDTO> dynamicContentDTOs) {
    if (dynamicContentDTOs == null) {
      return null;
    }

    List<DynamicContentResponse> list =
        new ArrayList<DynamicContentResponse>(dynamicContentDTOs.size());
    for (DynamicContentDTO dynamicContentDTO : dynamicContentDTOs) {
      list.add(toDynamicContentResponse(dynamicContentDTO));
    }

    return list;
  }
}
