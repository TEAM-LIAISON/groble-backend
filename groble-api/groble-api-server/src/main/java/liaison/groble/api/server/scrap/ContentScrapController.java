package liaison.groble.api.server.scrap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.scrap.request.UpdateContentScrapStateRequest;
import liaison.groble.api.model.scrap.response.UpdateContentScrapStateResponse;
import liaison.groble.api.model.scrap.response.swagger.UpdateContentScrapState;
import liaison.groble.api.server.scrap.mapper.ContentScrapDtoMapper;
import liaison.groble.application.scrap.dto.ContentScrapDto;
import liaison.groble.application.scrap.service.ContentScrapService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/scrap")
@Tag(name = "스크랩 관련 API", description = "스크랩 관련 API")
public class ContentScrapController {
  private final ContentScrapService contentScrapService;
  private final ContentScrapDtoMapper contentScrapDtoMapper;

  public ContentScrapController(
      ContentScrapService contentScrapService, ContentScrapDtoMapper contentScrapDtoMapper) {
    this.contentScrapService = contentScrapService;
    this.contentScrapDtoMapper = contentScrapDtoMapper;
  }

  @UpdateContentScrapState
  @PostMapping("/content/{contentId}")
  public ResponseEntity<GrobleResponse<UpdateContentScrapStateResponse>> scrapContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable(name = "contentId") Long contentId,
      @RequestBody final UpdateContentScrapStateRequest updateContentScrapStateRequest) {
    ContentScrapDto contentScrapDto =
        contentScrapService.updateContentScrap(
            accessor.getUserId(), contentId, updateContentScrapStateRequest.isChangeScrapValue());
    UpdateContentScrapStateResponse updateContentScrapStateResponse =
        contentScrapDtoMapper.toUpdateContentScrapStateResponse(contentScrapDto);
    return ResponseEntity.ok(
        GrobleResponse.success(updateContentScrapStateResponse, "콘텐츠 스크랩 상태 변경이 완료되었습니다.", 200));
  }
}
