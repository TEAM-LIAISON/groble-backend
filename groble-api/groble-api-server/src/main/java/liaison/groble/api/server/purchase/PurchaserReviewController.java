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
import liaison.groble.application.purchase.exception.PurchaseAuthenticationRequiredException;
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
    name = "[🧾 통합 리뷰 관리] 회원/비회원 구매자 리뷰 작성, 수정, 삭제 API",
    description = "토큰 종류에 따라 회원/비회원을 자동 판단하여 구매한 콘텐츠에 대한 리뷰 작성, 수정, 삭제 기능을 제공합니다.")
public class PurchaserReviewController {
  // API 경로 상수화
  private static final String PURCHASER_REVIEW_ADD_PATH = "/{merchantUid}";
  private static final String PURCHASER_REVIEW_UPDATE_PATH = "/update/{reviewId}";
  private static final String PURCHASER_REVIEW_DELETE_PATH = "/delete/{reviewId}";

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
      summary = "[✅ 통합 리뷰 관리 - 리뷰 추가] 내가 구매한 콘텐츠 리뷰 추가",
      description = "토큰 종류에 따라 회원/비회원을 자동 판단하여 구매한 콘텐츠에 대해 리뷰를 추가합니다.")
  @Logging(
      item = "PurchaserReview",
      action = "addReview",
      includeParam = true,
      includeResult = true)
  @PostMapping(PURCHASER_REVIEW_ADD_PATH)
  public ResponseEntity<GrobleResponse<PurchaserContentReviewResponse>> addReview(
      @Auth(required = false) Accessor accessor,
      @PathVariable("merchantUid") String merchantUid,
      @RequestBody PurchaserContentReviewRequest purchaserContentReviewRequest) {

    PurchaserContentReviewDTO purchaserContentReviewDTO =
        purchaserContentReviewMapper.toPurchaserContentReviewDTO(purchaserContentReviewRequest);

    PurchaserContentReviewDTO addedReviewDTO;
    String userTypeInfo;

    // 토큰 종류에 따른 분기 처리
    if (accessor.isAuthenticated() && !accessor.isGuest()) {
      // 회원 리뷰 추가
      log.info("회원 리뷰 추가 - userId: {}, merchantUid: {}", accessor.getUserId(), merchantUid);
      addedReviewDTO =
          purchaserReviewService.addReviewUnified(
              accessor.getUserId(), null, merchantUid, purchaserContentReviewDTO);
      userTypeInfo = "회원";

    } else if (accessor.isGuest()) {
      // 비회원 리뷰 추가
      log.info("비회원 리뷰 추가 - guestUserId: {}, merchantUid: {}", accessor.getId(), merchantUid);
      addedReviewDTO =
          purchaserReviewService.addReviewUnified(
              null, accessor.getId(), merchantUid, purchaserContentReviewDTO);
      userTypeInfo = "비회원";

    } else {
      // 인증되지 않은 사용자
      throw PurchaseAuthenticationRequiredException.forPurchaseList();
    }

    PurchaserContentReviewResponse purchaserContentReviewResponse =
        purchaserContentReviewMapper.toPurchaserContentReviewResponse(addedReviewDTO);

    log.info("{} 리뷰 추가 완료 - merchantUid: {}", userTypeInfo, merchantUid);
    return responseHelper.success(
        purchaserContentReviewResponse,
        userTypeInfo + " " + PURCHASER_REVIEW_ADD_SUCCESS_MESSAGE,
        HttpStatus.CREATED);
  }

  @Operation(
      summary = "[✅ 통합 리뷰 관리 - 리뷰 수정] 내가 작성한 콘텐츠의 리뷰 수정",
      description = "토큰 종류에 따라 회원/비회원을 자동 판단하여 작성한 리뷰를 수정합니다.")
  @Logging(
      item = "PurchaserReview",
      action = "updateReview",
      includeParam = true,
      includeResult = true)
  @PostMapping(PURCHASER_REVIEW_UPDATE_PATH)
  public ResponseEntity<GrobleResponse<PurchaserContentReviewResponse>> updateReview(
      @Auth(required = false) Accessor accessor,
      @PathVariable("reviewId") Long reviewId,
      @RequestBody PurchaserContentReviewRequest purchaserContentReviewRequest) {

    PurchaserContentReviewDTO purchaserContentReviewDTO =
        purchaserContentReviewMapper.toPurchaserContentReviewDTO(purchaserContentReviewRequest);

    PurchaserContentReviewDTO updatedReviewDTO;
    String userTypeInfo;

    // 토큰 종류에 따른 분기 처리
    if (accessor.isAuthenticated() && !accessor.isGuest()) {
      // 회원 리뷰 수정
      log.info("회원 리뷰 수정 - userId: {}, reviewId: {}", accessor.getUserId(), reviewId);
      updatedReviewDTO =
          purchaserReviewService.updateReviewUnified(
              accessor.getUserId(), null, reviewId, purchaserContentReviewDTO);
      userTypeInfo = "회원";

    } else if (accessor.isGuest()) {
      // 비회원 리뷰 수정
      log.info("비회원 리뷰 수정 - guestUserId: {}, reviewId: {}", accessor.getId(), reviewId);
      updatedReviewDTO =
          purchaserReviewService.updateReviewUnified(
              null, accessor.getId(), reviewId, purchaserContentReviewDTO);
      userTypeInfo = "비회원";

    } else {
      // 인증되지 않은 사용자
      throw PurchaseAuthenticationRequiredException.forPurchaseList();
    }

    PurchaserContentReviewResponse purchaserContentReviewResponse =
        purchaserContentReviewMapper.toPurchaserContentReviewResponse(updatedReviewDTO);

    log.info("{} 리뷰 수정 완료 - reviewId: {}", userTypeInfo, reviewId);
    return responseHelper.success(
        purchaserContentReviewResponse,
        userTypeInfo + " " + PURCHASER_REVIEW_UPDATE_SUCCESS_MESSAGE,
        HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 통합 리뷰 관리 - 리뷰 삭제] 내가 작성한 콘텐츠의 리뷰 삭제",
      description = "토큰 종류에 따라 회원/비회원을 자동 판단하여 작성한 리뷰를 삭제합니다.")
  @Logging(
      item = "PurchaserReview",
      action = "deleteReview",
      includeParam = true,
      includeResult = true)
  @PostMapping(PURCHASER_REVIEW_DELETE_PATH)
  public ResponseEntity<GrobleResponse<Void>> deleteReview(
      @Auth(required = false) Accessor accessor, @PathVariable("reviewId") Long reviewId) {

    String userTypeInfo;

    // 토큰 종류에 따른 분기 처리
    if (accessor.isAuthenticated() && !accessor.isGuest()) {
      // 회원 리뷰 삭제
      log.info("회원 리뷰 삭제 - userId: {}, reviewId: {}", accessor.getUserId(), reviewId);
      purchaserReviewService.deleteReviewUnified(accessor.getUserId(), null, reviewId);
      userTypeInfo = "회원";

    } else if (accessor.isGuest()) {
      // 비회원 리뷰 삭제
      log.info("비회원 리뷰 삭제 - guestUserId: {}, reviewId: {}", accessor.getId(), reviewId);
      purchaserReviewService.deleteReviewUnified(null, accessor.getId(), reviewId);
      userTypeInfo = "비회원";

    } else {
      // 인증되지 않은 사용자
      throw PurchaseAuthenticationRequiredException.forPurchaseList();
    }

    log.info("{} 리뷰 삭제 완료 - reviewId: {}", userTypeInfo, reviewId);
    return responseHelper.success(
        null, userTypeInfo + " " + PURCHASER_REVIEW_DELETE_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
