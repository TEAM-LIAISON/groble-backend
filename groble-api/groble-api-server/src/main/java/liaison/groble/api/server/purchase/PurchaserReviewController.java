package liaison.groble.api.server.purchase;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.purchase.request.PurchaserContentReviewRequest;
import liaison.groble.api.model.purchase.response.PurchaserContentReviewResponse;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.purchase.service.PurchaserReviewService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.content.PurchaserContentReviewMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/purchase/review")
@RequiredArgsConstructor
@Tag(
    name = "[🧾 내 콘텐츠 - 구매 관리 - 리뷰 작성] 구매자가 구매한 콘텐츠에 대해 리뷰 작성, 수정, 삭제 API",
    description = "내가 구매한 콘텐츠에 대한 리뷰 작성, 수정, 삭제 기능을 제공합니다.")
public class PurchaserReviewController {
  // API 경로 상수화
  private static final String PURCHASER_REVIEW_ADD_PATH = "/{contentId}";
  private static final String PURCHASER_REVIEW_UPDATE_PATH = "/{contentId}/update/{reviewId}";
  private static final String PURCHASER_REVIEW_DELETE_PATH = "/{contentId}/delete/{reviewId}";

  // 응답 메시지 상수화
  private static final String PURCHASER_REVIEW_ADD_SUCCESS_MESSAGE = "구매자가 콘텐츠에 대한 리뷰 작성에 성공했습니다.";
  private static final String PURCHASER_REVIEW_UPDATE_SUCCESS_MESSAGE =
      "구매자가 콘텐츠에 대한 리뷰 수정에 성공했습니다.";
  private static final String PURCHASER_REVIEW_DELETE_SUCCESS_MESSAGE =
      "구매자가 콘텐츠에 대한 리뷰 삭제에 성공했습니다.";

  // Mapper
  private final PurchaserContentReviewMapper purchaserContentReviewMapper;
  // Service
  private final PurchaserReviewService purchaserReviewService;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[❌ 내 콘텐츠 - 구매 관리 - 리뷰 수정] 내가 작성한 콘텐츠의 리뷰 수정",
      description = "내가 구매한 콘텐츠에 대해서 작성한 리뷰를 수정합니다.")
  @Logging(
      item = "PurchaserReview",
      action = "updateReview",
      includeParam = true,
      includeResult = true)
  @PostMapping(PURCHASER_REVIEW_UPDATE_PATH)
  public ResponseEntity<GrobleResponse<PurchaserContentReviewResponse>> updateReview(
      @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @PathVariable("reviewId") Long reviewId,
      @RequestBody PurchaserContentReviewRequest purchaserContentReviewRequest) {

    PurchaserContentReviewDTO purchaserContentReviewDTO =
        purchaserContentReviewMapper.toPurchaserContentReviewDTO(purchaserContentReviewRequest);

    PurchaserContentReviewDTO updatedReviewDTO =
        purchaserReviewService.updateReview(
            accessor.getUserId(), contentId, reviewId, purchaserContentReviewDTO);
    PurchaserContentReviewResponse purchaserContentReviewResponse =
        purchaserContentReviewMapper.toPurchaserContentReviewResponse(updatedReviewDTO);
    return responseHelper.success(
        purchaserContentReviewResponse, PURCHASER_REVIEW_UPDATE_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[❌ 내 콘텐츠 - 구매 관리 - 리뷰 삭제] 내가 작성한 콘텐츠의 리뷰 삭제",
      description = "내가 구매한 콘텐츠에 대해서 작성한 리뷰를 삭제합니다.")
  @Logging(item = "Review", action = "deleteReview", includeParam = true, includeResult = true)
  @PostMapping(PURCHASER_REVIEW_DELETE_PATH)
  public ResponseEntity<GrobleResponse<Void>> deleteReview(
      @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @PathVariable("reviewId") Long reviewId) {
    purchaserReviewService.deleteReview(accessor.getUserId(), contentId, reviewId);
    return responseHelper.success(null, PURCHASER_REVIEW_DELETE_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
