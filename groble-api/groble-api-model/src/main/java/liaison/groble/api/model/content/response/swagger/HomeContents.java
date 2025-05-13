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
                    name = "코칭 콘텐츠 목록",
                    summary = "코칭 유형 콘텐츠 목록 예시",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "홈화면 코칭 콘텐츠 조회 성공",
                                                  "data": [
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
                                                    },
                                                    {
                                                      "contentId": 103,
                                                      "createdAt": "2025-04-25 09:20:00",
                                                      "title": "제품 디자인 컨설팅",
                                                      "thumbnailUrl": "https://example.com/thumbnail_coaching3.jpg",
                                                      "sellerName": "UX디자이너",
                                                      "lowestPrice": 80000,
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "contentId": 104,
                                                      "createdAt": "2025-04-20 16:45:00",
                                                      "title": "마케팅 전략 수립 코칭",
                                                      "thumbnailUrl": "https://example.com/thumbnail_coaching4.jpg",
                                                      "sellerName": "마케팅전문가",
                                                      "lowestPrice": 100000,
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "contentId": 105,
                                                      "createdAt": "2025-04-15 11:30:00",
                                                      "title": "앱 개발 프로젝트 자문",
                                                      "thumbnailUrl": "https://example.com/thumbnail_coaching5.jpg",
                                                      "sellerName": "시니어개발자",
                                                      "lowestPrice": 200000,
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """),
                @ExampleObject(
                    name = "자료 콘텐츠 목록",
                    summary = "자료 유형 콘텐츠 목록 예시",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "홈화면 자료 콘텐츠 조회 성공",
                                                  "data": [
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
                                                    },
                                                    {
                                                      "contentId": 203,
                                                      "createdAt": "2025-04-27 15:45:00",
                                                      "title": "UI/UX 디자인 가이드북",
                                                      "thumbnailUrl": "https://example.com/thumbnail_doc3.jpg",
                                                      "sellerName": "시니어디자이너",
                                                      "lowestPrice": 30000,
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "contentId": 204,
                                                      "createdAt": "2025-04-23 10:30:00",
                                                      "title": "마케팅 데이터 분석 템플릿",
                                                      "thumbnailUrl": "https://example.com/thumbnail_doc4.jpg",
                                                      "sellerName": "데이터분석가",
                                                      "lowestPrice": 18000,
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "contentId": 205,
                                                      "createdAt": "2025-04-18 14:10:00",
                                                      "title": "개발자 포트폴리오 제작 가이드",
                                                      "thumbnailUrl": "https://example.com/thumbnail_doc5.jpg",
                                                      "sellerName": "채용전문가",
                                                      "lowestPrice": 22000,
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(responseCode = "400", description = "잘못된 요청 (지원하지 않는 콘텐츠 타입)"),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음")
})
public @interface HomeContents {}
