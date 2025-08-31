package liaison.groble.api.server.purchase;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.purchase.request.PurchaserContentReviewRequest;
import liaison.groble.api.model.purchase.response.PurchaserContentReviewResponse;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.purchase.docs.PurchaseReviewSwaggerDocs;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.application.purchase.strategy.ReviewProcessorFactory;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.factory.UserContextFactory;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.content.PurchaserContentReviewMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping(ApiPaths.Purchase.REVIEW_BASE)
@Tag(
    name = PurchaseReviewSwaggerDocs.TAG_NAME,
    description = PurchaseReviewSwaggerDocs.TAG_DESCRIPTION)
public class PurchaserReviewController extends BaseController {

  // Factory
  private final ReviewProcessorFactory processorFactory;
  // Mapper
  private final PurchaserContentReviewMapper purchaserContentReviewMapper;

  public PurchaserReviewController(
      ResponseHelper responseHelper,
      ReviewProcessorFactory processorFactory,
      PurchaserContentReviewMapper purchaserContentReviewMapper) {
    super(responseHelper);
    this.processorFactory = processorFactory;
    this.purchaserContentReviewMapper = purchaserContentReviewMapper;
  }

  @Operation(
      summary = PurchaseReviewSwaggerDocs.PURCHASER_REVIEW_ADD_SUMMARY,
      description = PurchaseReviewSwaggerDocs.PURCHASER_REVIEW_ADD_DESCRIPTION)
  @Logging(
      item = "PurchaserReview",
      action = "addReview",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Purchase.ADD_REVIEW)
  public ResponseEntity<GrobleResponse<PurchaserContentReviewResponse>> addReview(
      @Auth(required = false) Accessor accessor,
      @PathVariable("merchantUid") String merchantUid,
      @RequestBody PurchaserContentReviewRequest purchaserContentReviewRequest) {

    PurchaserContentReviewDTO purchaserContentReviewDTO =
        purchaserContentReviewMapper.toPurchaserContentReviewDTO(purchaserContentReviewRequest);
    UserContext userContext = UserContextFactory.from(accessor);
    PurchaserContentReviewDTO addedReviewDTO =
        processorFactory
            .getProcessor(userContext)
            .addReview(userContext, merchantUid, purchaserContentReviewDTO);
    PurchaserContentReviewResponse response =
        purchaserContentReviewMapper.toPurchaserContentReviewResponse(addedReviewDTO);

    return success(
        response, ResponseMessages.Purchase.PURCHASE_REVIEW_ADD_SUCCESS, HttpStatus.CREATED);
  }

  @Operation(
      summary = PurchaseReviewSwaggerDocs.PURCHASER_REVIEW_UPDATE_SUMMARY,
      description = PurchaseReviewSwaggerDocs.PURCHASER_REVIEW_UPDATE_DESCRIPTION)
  @Logging(
      item = "PurchaserReview",
      action = "updateReview",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Purchase.UPDATE_REVIEW)
  public ResponseEntity<GrobleResponse<PurchaserContentReviewResponse>> updateReview(
      @Auth(required = false) Accessor accessor,
      @PathVariable("reviewId") Long reviewId,
      @RequestBody PurchaserContentReviewRequest purchaserContentReviewRequest) {

    PurchaserContentReviewDTO purchaserContentReviewDTO =
        purchaserContentReviewMapper.toPurchaserContentReviewDTO(purchaserContentReviewRequest);

    UserContext userContext = UserContextFactory.from(accessor);
    PurchaserContentReviewDTO reviewDTO =
        processorFactory
            .getProcessor(userContext)
            .updateReview(userContext, reviewId, purchaserContentReviewDTO);

    PurchaserContentReviewResponse response =
        purchaserContentReviewMapper.toPurchaserContentReviewResponse(reviewDTO);

    return success(response, ResponseMessages.Purchase.PURCHASE_REVIEW_UPDATE_SUCCESS);
  }

  @Operation(
      summary = PurchaseReviewSwaggerDocs.PURCHASER_REVIEW_DELETE_SUMMARY,
      description = PurchaseReviewSwaggerDocs.PURCHASER_REVIEW_DELETE_DESCRIPTION)
  @Logging(
      item = "PurchaserReview",
      action = "deleteReview",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Purchase.DELETE_REVIEW)
  public ResponseEntity<GrobleResponse<Void>> deleteReview(
      @Auth(required = false) Accessor accessor, @PathVariable("reviewId") Long reviewId) {
    UserContext userContext = UserContextFactory.from(accessor);

    processorFactory.getProcessor(userContext).deleteReview(userContext, reviewId);

    return success(null, ResponseMessages.Purchase.PURCHASE_REVIEW_DELETE_SUCCESS);
  }
}
