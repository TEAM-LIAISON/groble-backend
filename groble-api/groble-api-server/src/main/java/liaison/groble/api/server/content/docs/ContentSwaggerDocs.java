package liaison.groble.api.server.content.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

public final class ContentSwaggerDocs {
  private ContentSwaggerDocs() {}

  public static final String HOME_CONTENTS_SUMMARY = "[✅ 홈 화면 콘텐츠 조회]";
  public static final String HOME_CONTENTS_DESCRIPTION =
      "sortOrder가 0이 아닌 활성 콘텐츠를 sortOrder 내림차순으로 조회합니다.";

  public static final String HOME_CONTENTS_SUCCESS_EXAMPLE =
      """
{
  "status": "SUCCESS",
  "code": 200,
  "message": "홈화면 콘텐츠 조회에 성공하였습니다.",
  "data": {
      "items": [
      {
        "contentId": 501,
        "createdAt": "2025-05-05 11:00:00",
        "title": "비즈니스 모델 구축 코칭",
        "thumbnailUrl": "https://cdn.groble.io/contents/501-thumb.png",
        "sellerName": "비즈니스전문가",
        "lowestPrice": 120000,
        "categoryId": "COACHING_BRANDING",
        "contentType": "COACHING",
        "priceOptionLength": 2,
        "isAvailableForSale": true,
        "status": "ACTIVE",
        "isDeletable": false
      },
      {
        "contentId": 312,
        "createdAt": "2025-05-02 09:15:00",
        "title": "스타트업 사업계획서 템플릿 모음",
        "thumbnailUrl": "https://cdn.groble.io/contents/312-thumb.png",
        "sellerName": "창업멘토",
        "lowestPrice": 25000,
        "categoryId": "DOCUMENT_TEMPLATE",
        "contentType": "DOCUMENT",
        "priceOptionLength": 1,
        "isAvailableForSale": true,
        "status": "ACTIVE",
        "isDeletable": false
      }
    ]
  },
  "timestamp": "2025-05-14T04:52:27"
}
""";

  public static final String TAG_NAME = SwaggerTags.Content.MANAGEMENT;
  public static final String TAG_DESCRIPTION = SwaggerTags.Content.MANAGEMENT_DESC;
}
