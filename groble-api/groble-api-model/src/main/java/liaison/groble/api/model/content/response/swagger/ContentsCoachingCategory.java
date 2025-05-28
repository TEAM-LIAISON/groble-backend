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
@Operation(
    summary = "카테고리별 콘텐츠 조회",
    description =
        "카테고리 ID로 페이지 번호 기반으로 콘텐츠를 조회합니다. 반환되는 `items` 배열의 요소는 ContentPreviewCardResponse 입니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "카테고리별 콘텐츠 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentsCategoryApiResponse.class),
              examples =
                  @ExampleObject(
                      name = "카테고리별 콘텐츠 예시",
                      summary = "items 안에 ContentPreviewCardResponse 객체가 들어옵니다.",
                      value =
                          """
        {
          "status": "SUCCESS",
          "code": 200,
          "message": "카테고리별 콘텐츠 조회 성공",
          "data": {
            "items": [
              {
                "contentId": 101,
                "createdAt": "2025-05-01 14:30:00",
                "title": "비즈니스 모델 구축 코칭",
                "thumbnailUrl": "https://example.com/thumbnail_coaching1.jpg",
                "sellerName": "비즈니스전문가",
                "lowestPrice": 120000,
                "status": "ACTIVE"
              },
              {
                "contentId": 102,
                "createdAt": "2025-04-28 10:15:00",
                "title": "스타트업 IR 피칭 클래스",
                "thumbnailUrl": "https://example.com/thumbnail_coaching2.jpg",
                "sellerName": "투자유치코치",
                "lowestPrice": 150000,
                "status": "ACTIVE"
              }
            ],
            "pageInfo": {
              "currentPage": 0,
              "totalPages": 5,
              "pageSize": 12,
              "totalElements": 60,
              "first": true,
              "last": false,
              "empty": false
            },
            "meta": {
              "categoryId": 1,
              "categoryName": "비즈니스",
              "sortBy": "createdAt",
              "sortDirection": "desc"
            }
          },
          "timestamp": "2025-05-14 04:52:27"
        }
        """))),
  @ApiResponse(responseCode = "400", description = "잘못된 요청 (파라미터 오류)"),
  @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
  @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
})
public @interface ContentsCoachingCategory {}
