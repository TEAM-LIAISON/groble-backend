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
}
