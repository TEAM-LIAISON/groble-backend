package liaison.groble.api.server.sell;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.sell.request.ReplyContentRequest;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.application.sell.service.SellContentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.sell.SellMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sell/content/manage")
@RequiredArgsConstructor
@Tag(
    name = "[🎁 상품 관리의 판매 관리] 상품 관리 페이지 내 판매 관리 기능",
    description = "특정 콘텐츠 판매 정보에 대한 모든 기능을 제공합니다.")
public class SellContentController {

  // API 경로 상수화
  private static final String CONTENT_HOME_PATH = "/{contentId}";
  private static final String CONTENT_SELL_LIST_PATH = "/{contentId}/sell-list";
  private static final String CONTENT_SELL_DETAIL_PATH = "/{contentId}/sell-detail/{purchaseId}";
  private static final String CONTENT_REVIEW_LIST_PATH = "/{contentId}/review-list";
  private static final String CONTENT_REVIEW_DETAIL_PATH = "/{contentId}/review-detail/{reviewId}";
  private static final String DELETE_REVIEW_REQUEST_PATH = "/{reviewId}/review-delete-request";

  private static final String ADD_REVIEW_REPLY_PATH = "/{reviewId}/review-reply";
  private static final String UPDATE_REVIEW_REPLY_PATH = "/{reviewId}/review-reply/{replyId}";
  private static final String DELETE_REVIEW_REPLY_PATH =
      "/{reviewId}/review-reply/{replyId}/delete";

  // 응답 메시지 상수화
  private static final String CONTENT_HOME_SUCCESS_MESSAGE = "콘텐츠 판매 관리 페이지 조회에 성공하였습니다.";
  private static final String CONTENT_SELL_LIST_SUCCESS_MESSAGE = "콘텐츠 판매 리스트 조회에 성공하였습니다.";
  private static final String CONTENT_SELL_DETAIL_SUCCESS_MESSAGE = "콘텐츠 판매 상세 조회에 성공하였습니다.";
  private static final String CONTENT_REVIEW_LIST_SUCCESS_MESSAGE = "콘텐츠 리뷰 리스트 조회에 성공하였습니다.";
  private static final String CONTENT_REVIEW_DETAIL_SUCCESS_MESSAGE = "콘텐츠 리뷰 상세 조회에 성공하였습니다.";
  private static final String DELETE_REVIEW_REQUEST_SUCCESS_MESSAGE = "리뷰 삭제 요청에 성공하였습니다.";

  private static final String ADD_REVIEW_REPLY_SUCCESS_MESSAGE = "리뷰 답글 달기에 성공하였습니다.";
  private static final String UPDATE_REVIEW_REPLY_SUCCESS_MESSAGE = "리뷰 답글 수정에 성공하였습니다.";
  private static final String DELETE_REVIEW_REPLY_SUCCESS_MESSAGE = "리뷰 답글 삭제에 성공하였습니다.";

  // Service
  private final SellContentService sellContentService;

  // Mapper
  private final SellMapper sellMapper;

  // Helper
  private final ResponseHelper responseHelper;

  // TODO : 내 스토어 - 상품 관리 - 판매 관리
  //    @RequireRole("ROLE_SELLER")
  //    @Operation(
  //            summary = "[✅ 내 스토어 - 상품 관리 - 판매 관리] 판매 관리 페이지 조회",
  //            description = "특정 상품의 판매 관리, 상위 판매 리스트, 상위 리뷰 내역을 모두 조회합니다.")
  //    @GetMapping(CONTENT_HOME_PATH)
  //    public ResponseEntity<GrobleResponse<>> getContentHome(
  //            @Auth Accessor accessor,
  //            @PathVariable("contentId") Long contentId
  //    ) {
  //
  //    }
  // TODO : 내 스토어 - 상품 관리 - 판매 리스트 전체보기
  // TODO : 내 스토어 - 상품 관리 - 리뷰 내역 전체보기

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세
  @Operation(
      summary = "[❌ 내 스토어 - 상품 관리 - 리뷰 내역 상세] 리뷰 내역 상세보기 조회",
      description = "특정 상품에 남겨진 리뷰의  상세 정보를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "판매 관리에서 판매 리스트 상세 정보 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentReviewDetailResponse.class)))
  @RequireRole("ROLE_SELLER")
  @GetMapping(CONTENT_REVIEW_DETAIL_PATH)
  public ResponseEntity<GrobleResponse<ContentReviewDetailResponse>> getContentReviewDetail(
      @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @PathVariable("reviewId") Long reviewId) {

    ContentReviewDetailDTO contentReviewDetailDTO =
        sellContentService.getContentReviewDetail(accessor.getUserId(), contentId, reviewId);

    ContentReviewDetailResponse response =
        sellMapper.toContentReviewDetailResponse(contentReviewDetailDTO);
    return responseHelper.success(response, CONTENT_REVIEW_DETAIL_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 삭제 요청
  @Operation(
      summary = "[❌ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 삭제 요청] 리뷰 삭제 요청",
      description = "특정 상품에 달린 리뷰를 삭제 요청합니다.")
  @ApiResponse(responseCode = "200", description = "리뷰 삭제 요청에 성공하였습니다.")
  @RequireRole("ROLE_SELLER")
  @PostMapping(DELETE_REVIEW_REQUEST_PATH)
  public ResponseEntity<GrobleResponse<Void>> deleteReviewRequest(
      @Auth Accessor accessor, @PathVariable("reviewId") Long reviewId) {

    sellContentService.deleteReviewRequest(accessor.getUserId(), reviewId);
    return responseHelper.success(null, DELETE_REVIEW_REQUEST_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 달기 [해당 콘텐츠의 게시자만 가능]
  @Operation(
      summary = "[❌ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 달기] 리뷰 답글 달기",
      description = "특정 콘텐츠에 달린 리뷰에 답글을 작성합니다.")
  @ApiResponse(
      responseCode = "200",
      description = ADD_REVIEW_REPLY_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ReplyContentResponse.class)))
  @RequireRole("ROLE_SELLER")
  @PostMapping(ADD_REVIEW_REPLY_PATH)
  public ResponseEntity<GrobleResponse<ReplyContentResponse>> addReviewReply(
      @Auth Accessor accessor,
      @PathVariable("reviewId") Long reviewId,
      @RequestBody ReplyContentRequest request) {

    ReplyContentDTO replyContentDTO = sellMapper.toReplyContentDTO(request);

    ReplyContentDTO response =
        sellContentService.addReviewReply(accessor.getUserId(), reviewId, replyContentDTO);
    ReplyContentResponse replyContentResponse = sellMapper.toReplyContentResponse(response);
    return responseHelper.success(
        replyContentResponse, ADD_REVIEW_REPLY_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 댓글 수정
  @Operation(
      summary = "[❌ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 수정] 리뷰 답글 수정",
      description = "특정 콘텐츠에 달린 리뷰에 답글을 수정합니다.")
  @RequireRole("ROLE_SELLER")
  @GetMapping(UPDATE_REVIEW_REPLY_PATH)
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
    return responseHelper.success(
        replyContentResponse, UPDATE_REVIEW_REPLY_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 댓글 삭제
  @Operation(
      summary = "[❌ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 수정] 리뷰 답글 삭제",
      description = "특정 콘텐츠에 달린 리뷰에 답글을 삭제합니다.")
  @RequireRole("ROLE_SELLER")
  @GetMapping(DELETE_REVIEW_REPLY_PATH)
  public ResponseEntity<GrobleResponse<Void>> deleteReviewReply(
      @Auth Accessor accessor,
      @PathVariable("reviewId") Long reviewId,
      @PathVariable("replyId") Long replyId) {
    sellContentService.deleteReviewReply(accessor.getUserId(), reviewId, replyId);
    return responseHelper.success(null, DELETE_REVIEW_REPLY_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 내 스토어 - 상품 관리 - 판매 관리 - 판매 리스트 상세
  @Operation(
      summary = "[❌ 내 스토어 - 상품 관리 - 판매 관리 - 판매 리스트 상세보기] 판매 리스트 상세보기 조회",
      description = "특정 상품의 판매 상세 정보를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "판매 관리에서 판매 리스트 상세 정보 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentSellDetailResponse.class)))
  @RequireRole("ROLE_SELLER")
  @GetMapping(CONTENT_SELL_DETAIL_PATH)
  public ResponseEntity<GrobleResponse<ContentSellDetailResponse>> getContentSellDetail(
      @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @PathVariable("purchaseId") Long purchaseId) {
    ContentSellDetailDTO contentSellDetailDTO =
        sellContentService.getContentSellDetail(accessor.getUserId(), contentId, purchaseId);
    ContentSellDetailResponse response =
        sellMapper.toContentSellDetailResponse(contentSellDetailDTO);
    return responseHelper.success(response, CONTENT_SELL_DETAIL_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
