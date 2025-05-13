package liaison.groble.api.server.purchase;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.purchase.swagger.MyPurchaseContents;
import liaison.groble.api.server.content.mapper.ContentDtoMapper;
import liaison.groble.application.content.dto.ContentCardDto;
import liaison.groble.application.purchase.PurchaseService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.request.CursorRequest;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/purchase")
@Tag(name = "구매 관련 API", description = "내가 구매한 콘텐츠 조회, 내가 구매한 콘텐츠(자료) 다운로드 등")
public class PurchaseController {
  private final PurchaseService purchaseService;
  private final ContentDtoMapper contentDtoMapper;

  public PurchaseController(PurchaseService purchaseService, ContentDtoMapper contentDtoMapper) {
    this.purchaseService = purchaseService;
    this.contentDtoMapper = contentDtoMapper;
  }

  @MyPurchaseContents
  @GetMapping("/contents/my")
  public ResponseEntity<GrobleResponse<CursorResponse<ContentPreviewCardResponse>>>
      getMyPurchasingContents(
          @Parameter(hidden = true) @Auth Accessor accessor,
          @Parameter(
                  description = "커서 기반 페이지네이션 요청 정보",
                  required = true,
                  schema = @Schema(implementation = CursorRequest.class))
              @Valid
              @ModelAttribute
              CursorRequest cursorRequest,
          @Parameter(
                  description = "구매한 콘텐츠 상태 필터 (PENDING, PAID, EXPIRED, CANCELLED)",
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"PENDING", "PAID", "EXPIRED", "CANCELLED"}))
              @RequestParam(value = "state", required = false)
              String state,
          @Parameter(
                  description = "콘텐츠 타입 (COACHING 또는 DOCUMENT)",
                  required = true,
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"COACHING", "DOCUMENT"}))
              @RequestParam(value = "type")
              String type) {
    CursorResponse<ContentCardDto> cardDtos =
        purchaseService.getMyPurchasingContents(
            accessor.getUserId(), cursorRequest.getCursor(), cursorRequest.getSize(), state, type);

    // DTO 변환
    List<ContentPreviewCardResponse> responseItems =
        cardDtos.getItems().stream()
            .map(contentDtoMapper::toContentPreviewCardFromCardDto)
            .toList();

    // CursorResponse 생성
    CursorResponse<ContentPreviewCardResponse> response =
        CursorResponse.<ContentPreviewCardResponse>builder()
            .items(responseItems)
            .nextCursor(cardDtos.getNextCursor())
            .hasNext(cardDtos.isHasNext())
            .totalCount(cardDtos.getTotalCount())
            .meta(cardDtos.getMeta())
            .build();

    String successMessage = "COACHING".equals(type) ? "내가 구매한 코칭 콘텐츠 조회 성공" : "내가 구매한 자료 콘텐츠 조회 성공";
    return ResponseEntity.ok(GrobleResponse.success(response, successMessage));
  }
}
