package liaison.groble.api.model.purchase.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "나의 구매 콘텐츠 조회", description = "내가 구매한 코칭 또는 자료 콘텐츠를 조회합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "나의 구매 콘텐츠 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = MyPurchasingContentsApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "내가 구매한 코칭 콘텐츠 조회 성공",
                    summary = "내가 구매한 코칭 콘텐츠 목록 조회",
                    value =
                        """
                                                                        {
                                                                          "status": "SUCCESS",
                                                                          "code": 200,
                                                                          "message": "내가 구매한 코칭 콘텐츠 조회 성공",
                                                                          "data": {
                                                                            "items": [
                                                                              {
                                                                                "contentId": 1,
                                                                                "createdAt": "2025-05-07 16:52:12",
                                                                                "title": "사업계획서 컨설팅",
                                                                                "thumbnailUrl": "https://example.com/image1.jpg",
                                                                                "sellerName": "권동민",
                                                                                "originalPrice": 50000,
                                                                                "status": "ACTIVE"
                                                                              },
                                                                              {
                                                                                "contentId": 2,
                                                                                "createdAt": "2025-05-07 15:30:45",
                                                                                "title": "스타트업 진단 코칭",
                                                                                "thumbnailUrl": "https://example.com/image2.jpg",
                                                                                "sellerName": "권동민",
                                                                                "originalPrice": 70000,
                                                                                "status": "PENDING"
                                                                              }
                                                                            ],
                                                                            "nextCursor": "2",
                                                                            "hasNext": true,
                                                                            "totalCount": 10,
                                                                            "meta": {
                                                                              "filter": "ACTIVE",
                                                                              "cursorType": "id"
                                                                            }
                                                                          },
                                                                          "timestamp": "2025-05-07 18:56:50"
                                                                        }
                                                                        """),
                @ExampleObject(
                    name = "자료 콘텐츠 조회 성공",
                    summary = "나의 자료 콘텐츠 목록 조회",
                    value =
                        """
                                                                        {
                                                                          "status": "SUCCESS",
                                                                          "code": 200,
                                                                          "message": "나의 자료 콘텐츠 조회 성공",
                                                                          "data": {
                                                                            "items": [
                                                                              {
                                                                                "contentId": 3,
                                                                                "createdAt": "2025-05-06 14:22:33",
                                                                                "title": "사업계획서 템플릿",
                                                                                "thumbnailUrl": "https://example.com/doc1.jpg",
                                                                                "sellerName": "권동민",
                                                                                "originalPrice": 20000,
                                                                                "status": "ACTIVE"
                                                                              },
                                                                              {
                                                                                "contentId": 4,
                                                                                "createdAt": "2025-05-05 09:15:21",
                                                                                "title": "투자 유치 제안서",
                                                                                "thumbnailUrl": "https://example.com/doc2.jpg",
                                                                                "sellerName": "권동민",
                                                                                "originalPrice": 15000,
                                                                                "status": "DRAFT"
                                                                              }
                                                                            ],
                                                                            "hasNext": false,
                                                                            "totalCount": 4,
                                                                            "meta": {
                                                                              "filter": "DOCUMENT",
                                                                              "cursorType": "id"
                                                                            }
                                                                          },
                                                                          "timestamp": "2025-05-07 18:56:50"
                                                                        }
                                                                        """)
              })),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
})
public @interface MyPurchasingContents {}
