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

import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.model.purchase.response.PurchasedContentDetailResponse;
import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.api.model.purchase.swagger.PurchasedContentsListResponse;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailDTO;
import liaison.groble.application.purchase.strategy.PurchaseProcessorFactory;
import liaison.groble.application.purchase.strategy.PurchaseProcessorStrategy;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.factory.UserContextFactory;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.market.MarketMapper;
import liaison.groble.mapping.purchase.PurchaseMapper;
import liaison.groble.mapping.sell.SellMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/purchase")
@RequiredArgsConstructor
@Tag(
    name = "[🧾 통합 구매 관리] 회원/비회원 구매 콘텐츠 조회, 다운로드, 문의하기 API",
    description = "토큰 종류에 따라 회원/비회원을 자동 판단하여 구매한 콘텐츠 (상세)조회, 다운로드, 문의하기 등을 처리합니다.")
public class PurchaseController {

  // API 경로 상수화
  private static final String SELLER_CONTACT_INFO_PATH = "/inquiry/{merchantUid}";
  private static final String MY_PURCHASING_CONTENT_PATH = "/content/my/purchased-contents";
  private static final String MY_PURCHASED_CONTENT_PATH = "/content/my/{merchantUid}";
  private static final String MY_PURCHASED_CONTENT_REVIEW_PATH = "/content/my/{merchantUid}/review";

  // 응답 메시지 상수화
  private static final String SELLER_CONTACT_INFO_SUCCESS_MESSAGE =
      "판매자(메이커)에게 문의하기 정보 조회에 성공했습니다.";
  private static final String MY_PURCHASING_CONTENT_SUCCESS_MESSAGE = "내가 구매한 콘텐츠 목록 조회에 성공했습니다.";
  private static final String My_PURCHASED_CONTENT_SUCCESS_MESSAGE = "내가 구매한 콘텐츠 상세 조회에 성공했습니다.";
  private static final String My_PURCHASED_CONTENT_REVIEW_SUCCESS_MESSAGE =
      "내가 구매한 콘텐츠 리뷰 상세 조회에 성공했습니다.";

  // Factory
  private final PurchaseProcessorFactory processorFactory;

  // Mapper
  private final PurchaseMapper purchaseMapper;
  private final MarketMapper marketMapper;
  private final SellMapper sellMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[✅ 내 콘텐츠 - 구매 관리] 내가 구매한 콘텐츠의 판매자(메이커)에게 문의하기 버튼 액션",
      description = "내가 구매한 콘텐츠의 판매자에게 문의하기 버튼을 클릭했을 때의 액션입니다. 오픈 채팅, 인스타그램, 이메일 등 값이 반환됩니다.")
  @GetMapping(SELLER_CONTACT_INFO_PATH)
  @Logging(
      item = "Purchase",
      action = "getSellerContactInfo",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<ContactInfoResponse>> getSellerContactInfo(
      @Auth(required = false) Accessor accessor,
      @Valid @PathVariable("merchantUid") String merchantUid) {

    UserContext userContext = UserContextFactory.from(accessor);
    PurchaseProcessorStrategy processor = processorFactory.getProcessor(userContext);
    ContactInfoDTO contactInfoDTO = processor.getContactInfo(userContext, merchantUid);
    ContactInfoResponse response = marketMapper.toContactInfoResponse(contactInfoDTO);

    String userTypeInfo = userContext.isMember() ? "회원" : "비회원";
    log.info(
        "{} 판매자 연락처 조회 - userId: {}, merchantUid: {}",
        userTypeInfo,
        userContext.getId(),
        merchantUid);

    return responseHelper.success(response, SELLER_CONTACT_INFO_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[❌ 내 콘텐츠 - 구매 관리] 내가 구매한 콘텐츠 상세 조회 (결제완료/결제취소요청/환불완료)",
      description = "내가 구매한 콘텐츠의 상세 정보를 조회합니다. 구매 상태에 따라 콘텐츠 접근 권한이 다를 수 있습니다.")
  @ApiResponse(
      responseCode = "200",
      description = My_PURCHASED_CONTENT_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PurchasedContentDetailResponse.class)))
  @GetMapping(MY_PURCHASED_CONTENT_PATH)
  @Logging(
      item = "Purchase",
      action = "getMyPurchasedContent",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<PurchasedContentDetailResponse>> getMyPurchasedContent(
      @Auth(required = false) Accessor accessor,
      @Valid @PathVariable("merchantUid") String merchantUid) {

    UserContext userContext = UserContextFactory.from(accessor);
    PurchaseProcessorStrategy processor = processorFactory.getProcessor(userContext);

    // 구매 콘텐츠 정보 조회
    PurchasedContentDetailDTO purchasedContentDetailDTO =
        processor.getMyPurchasedContent(userContext, merchantUid);

    // 문의하기 정보 조회
    ContactInfoDTO contactInfoDTO = processor.getContactInfo(userContext, merchantUid);
    ContactInfoResponse contactInfoResponse = marketMapper.toContactInfoResponse(contactInfoDTO);

    // 리뷰 상세 정보 조회
    ContentReviewDetailDTO contentReviewDetailDTO =
        processor.getContentReviewDetail(userContext, merchantUid);
    ContentReviewDetailResponse contentReviewDetailResponse =
        sellMapper.toContentReviewDetailResponse(contentReviewDetailDTO);

    PurchasedContentDetailResponse response =
        purchaseMapper.toPurchasedContentDetailResponse(
            purchasedContentDetailDTO, contactInfoResponse, contentReviewDetailResponse);

    String userTypeInfo = userContext.isMember() ? "회원" : "비회원";
    log.info(
        "{} 구매 콘텐츠 상세 조회 - userId: {}, merchantUid: {}",
        userTypeInfo,
        userContext.getId(),
        merchantUid);

    return responseHelper.success(response, My_PURCHASED_CONTENT_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 내 콘텐츠 - 구매 관리] 내가 구매한 콘텐츠 리뷰 상세 조회",
      description = "내가 구매한 콘텐츠의 리뷰를 상세 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = My_PURCHASED_CONTENT_REVIEW_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PurchasedContentDetailResponse.class)))
  @GetMapping(MY_PURCHASED_CONTENT_REVIEW_PATH)
  @Logging(
      item = "Purchase",
      action = "getMyPurchasedContentReview",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<PurchasedContentDetailResponse>> getMyPurchasedContentReview(
      @Auth(required = false) Accessor accessor,
      @Valid @PathVariable("merchantUid") String merchantUid) {

    UserContext userContext = UserContextFactory.from(accessor);
    PurchaseProcessorStrategy processor = processorFactory.getProcessor(userContext);

    // 구매 콘텐츠 정보 조회
    PurchasedContentDetailDTO purchasedContentDetailDTO =
        processor.getMyPurchasedContent(userContext, merchantUid);

    // 리뷰 상세 정보 조회
    ContentReviewDetailDTO contentReviewDetailDTO =
        processor.getContentReviewDetail(userContext, merchantUid);
    ContentReviewDetailResponse contentReviewDetailResponse =
        sellMapper.toContentReviewDetailResponse(contentReviewDetailDTO);

    PurchasedContentDetailResponse response =
        purchaseMapper.toPurchasedContentDetailResponse(
            purchasedContentDetailDTO, null, contentReviewDetailResponse);

    String userTypeInfo = userContext.isMember() ? "회원" : "비회원";
    log.info(
        "{} 구매 콘텐츠 리뷰 상세 조회 - userId: {}, merchantUid: {}",
        userTypeInfo,
        userContext.getId(),
        merchantUid);

    return responseHelper.success(
        response, My_PURCHASED_CONTENT_REVIEW_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 통합 구매 관리] 내가 구매한 콘텐츠 목록 조회",
      description = "토큰 종류에 따라 회원/비회원을 자동 판단하여 내가 구매한 콘텐츠 목록을 조회합니다. 구매 상태에 따라 필터링할 수 있습니다.")
  @ApiResponse(
      responseCode = "200",
      description = "[내 콘텐츠 - 구매 관리] 주문 상태에 따른 내가 구매한 콘텐츠 목록 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PurchasedContentsListResponse.class)))
  @GetMapping(MY_PURCHASING_CONTENT_PATH)
  @Logging(
      item = "Purchase",
      action = "getMyPurchasedContents",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<PageResponse<PurchaserContentPreviewCardResponse>>>
      getMyPurchasedContents(
          @Parameter @Auth(required = false) Accessor accessor,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "9") int size,
          @RequestParam(value = "sort", defaultValue = "purchasedAt") String sort,
          @Parameter(
                  description = "구매한 콘텐츠 상태 필터 [PAID - 결제완료], [CANCEL - 취소/환불]",
                  schema =
                      @Schema(
                          implementation = String.class,
                          allowableValues = {"PAID", "CANCEL"}))
              @RequestParam(value = "state", required = false)
              String state) {

    Pageable pageable = PageUtils.createPageable(page, size, sort);

    UserContext userContext = UserContextFactory.from(accessor);
    PurchaseProcessorStrategy processor = processorFactory.getProcessor(userContext);
    PageResponse<PurchaseContentCardDTO> DTOPageResponse =
        processor.getMyPurchasedContents(userContext, state, pageable);

    PageResponse<PurchaserContentPreviewCardResponse> responsePage =
        purchaseMapper.toPurchaserContentPreviewCardResponsePage(DTOPageResponse);

    String userTypeInfo = userContext.isMember() ? "회원" : "비회원";
    log.info("{} 구매 목록 조회 - userId: {}, state: {}", userTypeInfo, userContext.getId(), state);

    return responseHelper.success(
        responsePage, userTypeInfo + " " + MY_PURCHASING_CONTENT_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
