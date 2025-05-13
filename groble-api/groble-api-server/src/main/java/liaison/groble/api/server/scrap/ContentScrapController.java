package liaison.groble.api.server.scrap;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.response.ContentScrapCardResponse;
import liaison.groble.api.model.scrap.request.UpdateContentScrapStateRequest;
import liaison.groble.api.model.scrap.response.UpdateContentScrapStateResponse;
import liaison.groble.api.model.scrap.response.swagger.ContentScrapCard;
import liaison.groble.api.model.scrap.response.swagger.UpdateContentScrapState;
import liaison.groble.api.server.scrap.mapper.ContentScrapDtoMapper;
import liaison.groble.application.scrap.dto.ContentScrapCardDto;
import liaison.groble.application.scrap.dto.ContentScrapDto;
import liaison.groble.application.scrap.service.ContentScrapService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.request.CursorRequest;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

  @ContentScrapCard
  @GetMapping("/contents")
  public ResponseEntity<GrobleResponse<CursorResponse<ContentScrapCardResponse>>>
      getMyScrapContents(
          @Parameter(hidden = true) @Auth Accessor accessor,
          @Parameter(
                  description = "커서 기반 페이지네이션 요청 정보",
                  required = true,
                  schema = @Schema(implementation = CursorRequest.class))
              @Valid
              @ModelAttribute
              CursorRequest cursorRequest,
          @Parameter(
                  description = "콘텐츠 타입 (COACHING 또는 DOCUMENT)",
                  required = true,
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"COACHING", "DOCUMENT"}))
              @RequestParam(value = "type")
              String type) {
    CursorResponse<ContentScrapCardDto> contentScrapCardDtos =
        contentScrapService.getMyScrapContents(
            accessor.getUserId(), cursorRequest.getCursor(), cursorRequest.getSize(), type);

    List<ContentScrapCardResponse> responseItems =
        contentScrapCardDtos.getItems().stream()
            .map(contentScrapDtoMapper::toContentScrapCardFromCardDto)
            .toList();

    CursorResponse<ContentScrapCardResponse> response =
        CursorResponse.<ContentScrapCardResponse>builder()
            .items(responseItems)
            .nextCursor(contentScrapCardDtos.getNextCursor())
            .hasNext(contentScrapCardDtos.isHasNext())
            .totalCount(contentScrapCardDtos.getTotalCount())
            .meta(contentScrapCardDtos.getMeta())
            .build();

    String successMessage = "COACHING".equals(type) ? "코칭 스크랩 리스트 조회 성공" : "자료 스크랩 리스트 조회 성공";

    return ResponseEntity.ok(GrobleResponse.success(response, successMessage));
  }
}
