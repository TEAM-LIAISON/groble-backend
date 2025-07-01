package liaison.groble.api.server.purchase;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.api.model.purchase.swagger.MyPurchasingContents;
import liaison.groble.application.purchase.PurchaseService;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailResponse;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.purchase.PurchaseMapper;

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

  // API 경로 상수화
  private static final String MY_PURCHASING_CONTENT_PATH = "/content/my/purchasing-contents";
  private static final String MY_PURCHASED_CONTENT_PATH = "/content/my/{merchantUid}";

  // 응답 메시지 상수화
  private static final String MY_PURCHASING_CONTENT_SUCCESS_MESSAGE = "내가 구매한 콘텐츠 목록 조회에 성공했습니다.";
  private static final String My_PURCHASED_CONTENT_SUCCESS_MESSAGE = "내가 구매한 콘텐츠 상세 조회에 성공했습니다.";

  // Service
  private final PurchaseService purchaseService;

  // Mapper
  private final PurchaseMapper purchaseMapper;

  // Helper
  private final ResponseHelper responseHelper;

  //  @Operation(
  //      summary = "내가 구매한 콘텐츠의 판매자에게 문의하기 버튼 액션",
  //      description =
  //          "내가 구매한 콘텐츠의 판매자에게 문의하기 버튼을 클릭했을 때의 액션입니다. email, 카카오 오픈톡방 링크, 별도 링크 등 STRING 값이
  // 반환됩니다.")
  //  @GetMapping("/inquiry/{merchantUid}")
  //  public ResponseEntity<GrobleResponse<PurchasedContentSellerContactResponse>> getSellerContact(
  //      @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {
  //    PurchasedContentSellerContactResponse response =
  //        purchaseService.getSellerContact(accessor.getUserId(), merchantUid);
  //    return ResponseEntity.ok(GrobleResponse.success(response));
  //  }

  @Operation(
      summary = "[❌ 내 콘텐츠 - 구매 관리] 내가 구매한 콘텐츠 상세 조회 (결제완료/결제취소요청/환불완료)",
      description = "내가 구매한 콘텐츠의 상세 정보를 조회합니다. 구매 상태에 따라 콘텐츠 접근 권한이 다를 수 있습니다.")
  @GetMapping(MY_PURCHASED_CONTENT_PATH)
  public ResponseEntity<GrobleResponse<PurchasedContentDetailResponse>> getMyPurchasedContent(
      @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {
    PurchasedContentDetailResponse response =
        purchaseService.getMyPurchasedContent(accessor.getUserId(), merchantUid);

    return responseHelper.success(response, My_PURCHASED_CONTENT_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @MyPurchasingContents
  @GetMapping(MY_PURCHASING_CONTENT_PATH)
  public ResponseEntity<GrobleResponse<PageResponse<PurchaserContentPreviewCardResponse>>>
      getMyPurchasingContents(
          @Parameter @Auth Accessor accessor,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "12") int size,
          @RequestParam(value = "sort", defaultValue = "purchasedAt") String sort,
          @Parameter(
                  description = "구매한 콘텐츠 상태 필터 [PAID - 결제완료], [EXPIRED - 기간만료], [CANCELLED - 결제취소]",
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"PAID", "EXPIRED", "CANCELLED"}))
              @RequestParam(value = "state", required = false)
              String state) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<PurchaseContentCardDTO> dtoPageResponse =
        purchaseService.getMyPurchasedContents(accessor.getUserId(), state, pageable);

    PageResponse<PurchaserContentPreviewCardResponse> responsePage =
        purchaseMapper.toPurchaserContentPreviewCardResponsePage(dtoPageResponse);

    return responseHelper.success(
        responsePage, MY_PURCHASING_CONTENT_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
