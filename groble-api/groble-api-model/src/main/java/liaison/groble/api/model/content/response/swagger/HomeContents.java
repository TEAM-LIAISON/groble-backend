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
@Operation(summary = "홈화면 콘텐츠 목록 조회", description = "홈화면에 표시할 콘텐츠 목록을 타입별로 조회합니다. [코칭 또는 자료]")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "홈화면 콘텐츠 목록 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = HomeContentsApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "코칭+자료 콘텐츠 예시",
                    summary = "코칭과 자료 아이템이 모두 있는 경우",
                    value =
                        """
          {
            "status": "SUCCESS",
            "code": 200,
            "message": "홈화면 콘텐츠 조회 성공",
            "data": {
              "coachingItems": [
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
              "documentItems": [
                {
                  "contentId": 201,
                  "createdAt": "2025-05-03 09:15:00",
                  "title": "스타트업 사업계획서 템플릿 모음",
                  "thumbnailUrl": "https://example.com/thumbnail_doc1.jpg",
                  "sellerName": "창업멘토",
                  "lowestPrice": 25000,
                  "status": "ACTIVE"
                },
                {
                  "contentId": 202,
                  "createdAt": "2025-04-30 13:20:00",
                  "title": "프리랜서 계약서 패키지",
                  "thumbnailUrl": "https://example.com/thumbnail_doc2.jpg",
                  "sellerName": "법률전문가",
                  "lowestPrice": 15000,
                  "status": "ACTIVE"
                }
              ]
            },
            "timestamp": "2025-05-14 04:52:27"
          }
          """)
              })),
  @ApiResponse(responseCode = "400", description = "잘못된 요청 (지원하지 않는 콘텐츠 타입)"),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음")
})
public @interface HomeContents {}
