package liaison.groble.api.server.sell.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

public class SellContentSwaggerDocs {

  private SellContentSwaggerDocs() {}

  // === 공통 태그 ===
  public static final String TAG_NAME = SwaggerTags.Sell.SELL_CONTENT;
  public static final String TAG_DESCRIPTION = SwaggerTags.Sell.SELL_CONTENT_DESC;

  // === 판매 관리 페이지 조회 ===
  public static final String SELL_CONTENT_HOME = "[✅ 내 스토어 - 상품 관리 - 판매 관리] 판매 관리 메인 페이지 조회";
  public static final String SELL_CONTENT_HOME_DESC =
      """
                    특정 상품의 판매 관리, 상위 판매 리스트, 상위 리뷰 내역을 모두 조회합니다.
                    """;

  // === 판매 콘텐츠 리스트 조회 ===
  public static final String SELL_CONTENT_LIST = "[✅ 내 스토어 - 상품 관리 - 판매 콘텐츠 리스트] 판매 콘텐츠 리스트 조회";
  public static final String SELL_CONTENT_LIST_DESC =
      """
                      판매 중인 콘텐츠의 리스트를 조회합니다.
                      """;

  // === 판매 콘텐츠 상세 조회 ===
  public static final String SELL_CONTENT_DETAIL =
      "[✅ 내 스토어 - 상품 관리 - 판매 관리 - 판매 리스트 상세보기] 판매 리스트 상세보기 조회";
  public static final String SELL_CONTENT_DETAIL_DESC =
      """
                      특정 판매 콘텐츠의 상세 정보를 조회합니다.
                      """;

  // === 판매 콘텐츠 리뷰 리스트 조회 ===
  public static final String SELL_CONTENT_REVIEW_LIST =
      "[✅ 내 스토어 - 상품 관리 - 판매 관리 - 리뷰 관리] 판매 콘텐츠 리뷰 리스트 조회";
  public static final String SELL_CONTENT_REVIEW_LIST_DESC =
      """
                        특정 판매 콘텐츠에 대한 리뷰 리스트를 조회합니다.
                        """;
  // === 판매 콘텐츠 리뷰 상세 조회 ===
  public static final String SELL_CONTENT_REVIEW_DETAIL =
      "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세] 리뷰 내역 상세보기 조회";
  public static final String SELL_CONTENT_REVIEW_DETAIL_DESC =
      """
                        특정 상품에 남겨진 리뷰의 상세 정보를 조회합니다.
                        """;

  // === 판매 콘텐츠 리뷰 삭제 ===
  public static final String DELETE_REVIEW_REQUEST =
      "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 삭제 요청] 리뷰 삭제 요청";
  public static final String DELETE_REVIEW_REQUEST_DESC =
      """
                        특정 콘텐츠에 달린 리뷰를 삭제를 요청합니다.
                        """;

  // === 리뷰 답글 작성 ===
  public static final String REVIEW_REPLY_ADD = "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 달기] 리뷰 답글 달기";
  public static final String REVIEW_REPLY_ADD_DESC =
      """
                            특정 콘텐츠에 달린 리뷰에 답글을 작성합니다.
                            """;

  // === 리뷰 답글 수정 ===
  public static final String REVIEW_REPLY_UPDATE =
      "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 수정] 리뷰 답글 수정";
  public static final String REVIEW_REPLY_UPDATE_DESC =
      """
                        특정 콘텐츠에 달린 리뷰 답글을 수정합니다.
                        """;

  // === 리뷰 답글 삭제 ===
  public static final String REVIEW_REPLY_DELETE =
      "[✅ 내 스토어 - 상품 관리 - 리뷰 내역 상세 - 리뷰 답글 삭제] 리뷰 답글 삭제";

  public static final String REVIEW_REPLY_DELETE_DESC =
      """
                        특정 콘텐츠에 달린 리뷰에 답글을 삭제합니다.
                        """;
}
