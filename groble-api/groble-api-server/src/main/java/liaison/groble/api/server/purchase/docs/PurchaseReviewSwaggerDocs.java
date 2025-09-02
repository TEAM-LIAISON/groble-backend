package liaison.groble.api.server.purchase.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

public class PurchaseReviewSwaggerDocs {
  private PurchaseReviewSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Purchase.REVIEW;
  public static final String TAG_DESCRIPTION = SwaggerTags.Purchase.REVIEW_DESC;

  // === 리뷰 작성 요청 API ===
  public static final String PURCHASER_REVIEW_ADD_SUMMARY =
      "[✅ 통합 리뷰 관리 - 구매 리뷰 작성] 회원/비회원 구매자가 콘텐츠에 대한 리뷰를 작성합니다.";
  public static final String PURCHASER_REVIEW_ADD_DESCRIPTION =
      """
            구매자가 콘텐츠에 대한 리뷰를 작성합니다.

            **주의사항:**
            - 리뷰 작성은 해당 콘텐츠를 구매한 사용자만 가능합니다.
            - 리뷰 작성 후에는 수정 및 삭제가 가능합니다.
            - 리뷰 작성 시 별점과 내용을 함께 제공해야 합니다.
            - 회원 로그인 또는 비회원 인증이 필요합니다.
            """;

  // === 리뷰 수정 요청 API ===
  public static final String PURCHASER_REVIEW_UPDATE_SUMMARY =
      "[✅ 통합 리뷰 관리 - 구매 리뷰 수정] 회원/비회원 구매자가 콘텐츠에 대한 리뷰를 수정합니다.";
  public static final String PURCHASER_REVIEW_UPDATE_DESCRIPTION =
      """
            구매자가 작성한 리뷰를 수정합니다.

            **주의사항:**
            - 리뷰 수정은 본인이 작성한 리뷰에 한해 가능합니다.
            - 리뷰 수정 시 별점과 내용을 함께 제공해야 합니다.
            - 회원 로그인 또는 비회원 인증이 필요합니다.
            """;
  // === 리뷰 삭제 요청 API ===
  public static final String PURCHASER_REVIEW_DELETE_SUMMARY =
      "[✅ 통합 리뷰 관리 - 구매 리뷰 삭제] 회원/비회원 구매자가 콘텐츠에 대한 리뷰를 삭제합니다.";
  public static final String PURCHASER_REVIEW_DELETE_DESCRIPTION =
      """
            구매자가 작성한 리뷰를 삭제합니다.

            **주의사항:**
            - 리뷰 삭제는 본인이 작성한 리뷰에 한해 가능합니다.
            - 삭제된 리뷰는 복구할 수 없습니다.
            - 회원 로그인 또는 비회원 인증이 필요합니다.
            """;
}
