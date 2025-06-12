package liaison.groble.api.server.purchase;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.api.model.purchase.swagger.MyPurchasingContents;
import liaison.groble.api.server.purchase.mapper.PurchaseDtoMapper;
import liaison.groble.application.purchase.PurchaseService;
import liaison.groble.application.purchase.dto.PurchaseContentCardDto;
import liaison.groble.application.purchase.dto.PurchasedContentDetailResponse;
import liaison.groble.application.purchase.dto.PurchasedContentSellerContactResponse;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.request.CursorRequest;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/purchase")
@RequiredArgsConstructor
@Tag(name = "구매 관련 API", description = "내가 구매한 콘텐츠 조회, 내가 구매한 콘텐츠(자료) 다운로드 등")
public class PurchaseController {
  private final PurchaseService purchaseService;
  private final PurchaseDtoMapper purchaseDtoMapper;

  @Operation(
      summary = "내가 구매한 콘텐츠의 판매자에게 문의하기 버튼 액션",
      description =
          "내가 구매한 콘텐츠의 판매자에게 문의하기 버튼을 클릭했을 때의 액션입니다. email, 카카오 오픈톡방 링크, 별도 링크 등 STRING 값이 반환됩니다.")
  @GetMapping("/inquiry/{merchantUid}")
  public ResponseEntity<GrobleResponse<PurchasedContentSellerContactResponse>> getSellerContact(
      @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {
    PurchasedContentSellerContactResponse response =
        purchaseService.getSellerContact(accessor.getUserId(), merchantUid);
    return ResponseEntity.ok(GrobleResponse.success(response));
  }

  @Operation(
      summary = "내가 구매한 콘텐츠 상세 조회",
      description = "내가 구매한 콘텐츠의 상세 정보를 조회합니다. 구매 상태에 따라 콘텐츠 접근 권한이 다를 수 있습니다.")
  @GetMapping("/content/my/{merchantUid}")
  public ResponseEntity<GrobleResponse<PurchasedContentDetailResponse>> getMyPurchasedContent(
      @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {
    PurchasedContentDetailResponse response =
        purchaseService.getMyPurchasedContent(accessor.getUserId(), merchantUid);

    return ResponseEntity.ok(GrobleResponse.success(response));
  }

  @MyPurchasingContents
  @GetMapping("/content/my/purchasing-contents")
  public ResponseEntity<GrobleResponse<CursorResponse<PurchaserContentPreviewCardResponse>>>
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
                  description = "구매한 콘텐츠 상태 필터 [PAID - 결제완료], [EXPIRED - 기간만료], [CANCELLED - 결제취소]",
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"PAID", "EXPIRED", "CANCELLED"}))
              @RequestParam(value = "state")
              String state,
          @Parameter(
                  description = "콘텐츠 유형 [COACHING - 코칭], [DOCUMENT - 자료]",
                  required = true,
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"COACHING", "DOCUMENT"}))
              @RequestParam(value = "type")
              String type) {

    CursorResponse<PurchaseContentCardDto> purchaseCardDtos =
        purchaseService.getMyPurchasingContents(
            accessor.getUserId(), cursorRequest.getCursor(), cursorRequest.getSize(), state, type);

    // DTO 변환
    List<PurchaserContentPreviewCardResponse> responseItems =
        purchaseCardDtos.getItems().stream()
            .map(purchaseDtoMapper::toPurchaseContentPreviewCardFromCardDto)
            .toList();

    // CursorResponse 생성
    CursorResponse<PurchaserContentPreviewCardResponse> response =
        CursorResponse.<PurchaserContentPreviewCardResponse>builder()
            .items(responseItems)
            .nextCursor(purchaseCardDtos.getNextCursor())
            .hasNext(purchaseCardDtos.isHasNext())
            .totalCount(purchaseCardDtos.getTotalCount())
            .meta(purchaseCardDtos.getMeta())
            .build();

    String successMessage = "COACHING".equals(type) ? "내가 구매한 코칭 콘텐츠 조회 성공" : "내가 구매한 자료 콘텐츠 조회 성공";
    return ResponseEntity.ok(GrobleResponse.success(response, successMessage));
  }
}
