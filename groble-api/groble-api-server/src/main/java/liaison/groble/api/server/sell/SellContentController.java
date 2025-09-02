package liaison.groble.api.server.sell;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.sell.request.ReplyContentRequest;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.api.model.sell.response.SellManagePageResponse;
import liaison.groble.api.model.sell.response.swagger.ContentReviewListResponse;
import liaison.groble.api.model.sell.response.swagger.ContentSellListResponse;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.sell.docs.SellContentSwaggerDocs;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.application.sell.dto.SellManagePageDTO;
import liaison.groble.application.sell.service.SellContentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.sell.SellMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping(ApiPaths.Sell.SELL_CONTENT_BASE)
@Tag(name = SellContentSwaggerDocs.TAG_NAME, description = SellContentSwaggerDocs.TAG_DESCRIPTION)
public class SellContentController extends BaseController {
  // Service
  private final SellContentService sellContentService;

  // Mapper
  private final SellMapper sellMapper;

  public SellContentController(
      ResponseHelper responseHelper, SellContentService sellContentService, SellMapper sellMapper) {
    super(responseHelper);
    this.sellContentService = sellContentService;
    this.sellMapper = sellMapper;
  }

  @Operation(
      summary = SellContentSwaggerDocs.SELL_CONTENT_HOME,
      description = SellContentSwaggerDocs.SELL_CONTENT_HOME_DESC)
  @ApiResponse(
      responseCode = "200",
      description = "판매 관리 페이지 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SellManagePageResponse.class)))
  @RequireRole("ROLE_SELLER")
  @Logging(
      item = "SellContent",
      action = "getContentHome",
      includeParam = true,
      includeResult = true)
  @GetMapping(ApiPaths.Sell.CONTENT_HOME)
  public ResponseEntity<GrobleResponse<SellManagePageResponse>> getContentHome(
      @Auth Accessor accessor, @PathVariable("contentId") Long contentId) {
    SellManagePageDTO sellManagePageDTO =
        sellContentService.getSellManagePage(accessor.getId(), contentId);

    SellManagePageResponse response = sellMapper.toSellManagePageResponse(sellManagePageDTO);

    return success(response, ResponseMessages.Sell.SELL_CONTENT_HOME_SUCCESS);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 전체보기
  @Operation(
      summary = "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 전체보기] 리뷰 내역 전체보기 조회",
      description = "특정 상품에 남겨진 리뷰의 전체 정보를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "판매 관리에서 리뷰 리스트 전체 정보 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentReviewListResponse.class)))
  @RequireRole("ROLE_SELLER")
  @GetMapping(ApiPaths.Sell.CONTENT_REVIEW_LIST)
  public ResponseEntity<GrobleResponse<PageResponse<ContentReviewDetailResponse>>>
      getContentReviewList(
          @Auth Accessor accessor,
          @PathVariable("contentId") Long contentId,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "12") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt,popular") String sort) {

    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<ContentReviewDetailDTO> dtoPageResponse =
        sellContentService.getContentReviews(accessor.getId(), contentId, pageable);

    PageResponse<ContentReviewDetailResponse> responsePage =
        sellMapper.toContentReviewResponsePage(dtoPageResponse);
    return success(responsePage, ResponseMessages.Sell.SELL_CONTENT_REVIEW_LIST_SUCCESS);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세
  @Operation(
      summary = "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세] 리뷰 내역 상세보기 조회",
      description = "특정 상품에 남겨진 리뷰의  상세 정보를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "판매 관리에서 판매 리스트 상세 정보 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentReviewDetailResponse.class)))
  @RequireRole("ROLE_SELLER")
  @GetMapping(ApiPaths.Sell.CONTENT_REVIEW_DETAIL)
  public ResponseEntity<GrobleResponse<ContentReviewDetailResponse>> getContentReviewDetail(
      @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @PathVariable("reviewId") Long reviewId) {

    ContentReviewDetailDTO contentReviewDetailDTO =
        sellContentService.getContentReviewDetail(accessor.getUserId(), contentId, reviewId);

    ContentReviewDetailResponse response =
        sellMapper.toContentReviewDetailResponse(contentReviewDetailDTO);
    return success(response, ResponseMessages.Sell.SELL_CONTENT_REVIEW_DETAIL_SUCCESS);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 삭제 요청
  @Operation(
      summary = "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 삭제 요청] 리뷰 삭제 요청",
      description = "특정 상품에 달린 리뷰를 삭제 요청합니다.")
  @ApiResponse(responseCode = "200", description = "리뷰 삭제 요청에 성공하였습니다.")
  @RequireRole("ROLE_SELLER")
  @PostMapping(ApiPaths.Sell.DELETE_REVIEW_REQUEST)
  public ResponseEntity<GrobleResponse<Void>> deleteReviewRequest(
      @Auth Accessor accessor, @PathVariable("reviewId") Long reviewId) {

    sellContentService.deleteReviewRequest(accessor.getUserId(), reviewId);
    return success(null, ResponseMessages.Sell.SELL_CONTENT_REVIEW_DELETE_SUCCESS);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 달기 [해당 콘텐츠의 게시자만 가능]
  @Operation(
      summary = "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 달기] 리뷰 답글 달기",
      description = "특정 콘텐츠에 달린 리뷰에 답글을 작성합니다.")
  @ApiResponse(
      responseCode = "200",
      description = ResponseMessages.Sell.REVIEW_REPLY_ADD_SUCCESS,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ReplyContentResponse.class)))
  @RequireRole("ROLE_SELLER")
  @PostMapping(ApiPaths.Sell.ADD_REVIEW_REPLY)
  public ResponseEntity<GrobleResponse<ReplyContentResponse>> addReviewReply(
      @Auth Accessor accessor,
      @PathVariable("reviewId") Long reviewId,
      @RequestBody ReplyContentRequest request) {

    ReplyContentDTO replyContentDTO = sellMapper.toReplyContentDTO(request);

    ReplyContentDTO response =
        sellContentService.addReviewReply(accessor.getUserId(), reviewId, replyContentDTO);
    ReplyContentResponse replyContentResponse = sellMapper.toReplyContentResponse(response);
    return success(replyContentResponse, ResponseMessages.Sell.REVIEW_REPLY_ADD_SUCCESS);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 댓글 수정
  @Operation(
      summary = "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 수정] 리뷰 답글 수정",
      description = "특정 콘텐츠에 달린 리뷰에 답글을 수정합니다.")
  @RequireRole("ROLE_SELLER")
  @PostMapping(ApiPaths.Sell.UPDATE_REVIEW_REPLY)
  public ResponseEntity<GrobleResponse<ReplyContentResponse>> updateReviewReply(
      @Auth Accessor accessor,
      @PathVariable("reviewId") Long reviewId,
      @PathVariable("replyId") Long replyId,
      @RequestBody ReplyContentRequest request) {
    ReplyContentDTO replyContentDTO = sellMapper.toReplyContentDTO(request);
    ReplyContentDTO response =
        sellContentService.updateReviewReply(
            accessor.getUserId(), reviewId, replyId, replyContentDTO);
    ReplyContentResponse replyContentResponse = sellMapper.toReplyContentResponse(response);
    return success(replyContentResponse, ResponseMessages.Sell.REVIEW_REPLY_UPDATE_SUCCESS);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 댓글 삭제
  @Operation(
      summary = "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 삭제] 리뷰 답글 삭제",
      description = "특정 콘텐츠에 달린 리뷰에 답글을 삭제합니다.")
  @RequireRole("ROLE_SELLER")
  @PostMapping(ApiPaths.Sell.DELETE_REVIEW_REPLY)
  public ResponseEntity<GrobleResponse<Void>> deleteReviewReply(
      @Auth Accessor accessor,
      @PathVariable("reviewId") Long reviewId,
      @PathVariable("replyId") Long replyId) {
    sellContentService.deleteReviewReply(accessor.getUserId(), reviewId, replyId);
    return success(null, ResponseMessages.Sell.REVIEW_REPLY_DELETE_SUCCESS);
  }

  // TODO : 내 스토어 - 상품 관리 - 판매 리스트 전체보기
  @Operation(
      summary = "[✅ 내 스토어 - 상품 관리 - 판매 내역 전체보기] 판매 내역 전체보기 조회",
      description = "특정 상품의 판매 내역 전체 정보를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "판매 관리에서 판매 내역 전체 정보 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentSellListResponse.class)))
  @RequireRole("ROLE_SELLER")
  @GetMapping(ApiPaths.Sell.CONTENT_SELL_LIST)
  public ResponseEntity<GrobleResponse<PageResponse<ContentSellDetailResponse>>> getContentSellList(
      @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "12") int size,
      @RequestParam(value = "sort", defaultValue = "purchasedAt,desc") String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<ContentSellDetailDTO> dtoPageResponse =
        sellContentService.getContentSells(accessor.getId(), contentId, pageable);

    PageResponse<ContentSellDetailResponse> responsePage =
        sellMapper.toContentSellResponsePage(dtoPageResponse);
    return success(responsePage, ResponseMessages.Sell.SELL_CONTENT_LIST_SUCCESS);
  }

  // 내 스토어 - 상품 관리 - 판매 관리 - 판매 리스트 상세
  @Operation(
      summary = "[✅ 내 스토어 - 상품 관리 - 판매 관리 - 판매 리스트 상세보기] 판매 리스트 상세보기 조회",
      description = "특정 상품의 판매 상세 정보를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "판매 관리에서 판매 리스트 상세 정보 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentSellDetailResponse.class)))
  @RequireRole("ROLE_SELLER")
  @GetMapping(ApiPaths.Sell.CONTENT_SELL_DETAIL)
  public ResponseEntity<GrobleResponse<ContentSellDetailResponse>> getContentSellDetail(
      @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @PathVariable("purchaseId") Long purchaseId) {
    ContentSellDetailDTO contentSellDetailDTO =
        sellContentService.getContentSellDetail(accessor.getUserId(), contentId, purchaseId);
    ContentSellDetailResponse response =
        sellMapper.toContentSellDetailResponse(contentSellDetailDTO);
    return success(response, ResponseMessages.Sell.SELL_CONTENT_DETAIL_SUCCESS);
  }
}
