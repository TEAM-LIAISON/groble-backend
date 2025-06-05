package liaison.groble.api.server.global;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.response.DynamicContentListResponse;
import liaison.groble.api.model.content.response.DynamicContentResponse;
import liaison.groble.application.content.dto.DynamicContentDto;
import liaison.groble.application.content.service.ContentService;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "그로블 동적 데이터 API", description = "그로블 상품 동적 데이터 조회 API")
public class GrobleController {

  private final ContentService contentService;

  @GetMapping("/groble/contents")
  public ResponseEntity<GrobleResponse<DynamicContentListResponse>> getDynamicContentList() {
    List<DynamicContentDto> dynamicContentDtos = contentService.getDynamicContents();
    List<DynamicContentResponse> dynamicContentResponses =
        dynamicContentDtos.stream().map(this::convert).toList();

    DynamicContentListResponse response =
        DynamicContentListResponse.builder()
            .dynamicContentResponses(dynamicContentResponses)
            .build();
    return ResponseEntity.ok(GrobleResponse.success(response));
  }

  private DynamicContentResponse convert(DynamicContentDto flatDto) {
    return DynamicContentResponse.builder()
        .contentId(flatDto.getContentId())
        .title(flatDto.getTitle())
        .contentType(flatDto.getContentType())
        .thumbnailUrl(flatDto.getThumbnailUrl())
        .updatedAt(flatDto.getUpdatedAt())
        .build();
  }
}
