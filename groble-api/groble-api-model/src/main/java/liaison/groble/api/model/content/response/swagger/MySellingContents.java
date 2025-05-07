package liaison.groble.api.model.content.response.swagger;

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
@Operation(summary = "나의 판매 상품 조회", description = "나의 코칭 또는 자료 상품을 조회합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "나의 판매 상품 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = MySellingContentsApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "코칭 상품 조회 성공",
                    summary = "나의 코칭 상품 목록 조회",
                    value =
                        """
                                    {
                                      "status": "SUCCESS",
                                      "code": 200,
                                      "message": "나의 코칭 상품 조회 성공",
                                      "data": {
                                        "items": [
                                          {
                                            "id": 1,
                                            "title": "사업계획서 컨설팅",
                                            "contentType": "COACHING",
                                            "thumbnailUrl": "https://example.com/image1.jpg",
                                            "status": "ACTIVE",
                                            "price": 50000,
                                            "categoryName": "비즈니스 컨설팅"
                                          },
                                          {
                                            "id": 2,
                                            "title": "스타트업 진단 코칭",
                                            "contentType": "COACHING",
                                            "thumbnailUrl": "https://example.com/image2.jpg",
                                            "status": "PENDING_REVIEW",
                                            "price": 70000,
                                            "categoryName": "비즈니스 컨설팅"
                                          }
                                        ],
                                        "nextCursor": "cursor_token_123",
                                        "hasNext": true,
                                        "totalCount": 10,
                                        "meta": {
                                          "draftCount": 2,
                                          "pendingCount": 1,
                                          "activeCount": 7
                                        }
                                      },
                                      "timestamp": "2025-05-06 04:26:26"
                                    }
                                    """),
                @ExampleObject(
                    name = "자료 상품 조회 성공",
                    summary = "나의 자료 상품 목록 조회",
                    value =
                        """
                                    {
                                      "status": "SUCCESS",
                                      "code": 200,
                                      "message": "나의 자료 상품 조회 성공",
                                      "data": {
                                        "items": [
                                          {
                                            "id": 3,
                                            "title": "사업계획서 템플릿",
                                            "contentType": "DOCUMENT",
                                            "thumbnailUrl": "https://example.com/doc1.jpg",
                                            "status": "ACTIVE",
                                            "price": 20000,
                                            "categoryName": "사업계획서"
                                          },
                                          {
                                            "id": 4,
                                            "title": "투자 유치 제안서",
                                            "contentType": "DOCUMENT",
                                            "thumbnailUrl": "https://example.com/doc2.jpg",
                                            "status": "DRAFT",
                                            "price": 15000,
                                            "categoryName": "제안서"
                                          }
                                        ],
                                        "nextCursor": null,
                                        "hasNext": false,
                                        "totalCount": 4,
                                        "meta": {
                                          "draftCount": 1,
                                          "pendingCount": 0,
                                          "activeCount": 3
                                        }
                                      },
                                      "timestamp": "2025-05-06 04:26:26"
                                    }
                                    """)
              })),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
})
public @interface MySellingContents {}
