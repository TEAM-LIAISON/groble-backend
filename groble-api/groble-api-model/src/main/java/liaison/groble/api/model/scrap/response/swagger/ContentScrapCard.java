package liaison.groble.api.model.scrap.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "내가 스크랩한 콘텐츠 조회", description = "내가 스크랩한 콘텐츠들을 커서 기반 페이지네이션으로 조회합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "스크랩 콘텐츠 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentScrapCardApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "코칭 콘텐츠 스크랩 목록",
                    summary = "코칭 유형의 스크랩된 콘텐츠 목록",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "스크랩 콘텐츠 조회 성공",
                                                  "data": {
                                                    "items": [
                                                      {
                                                        "contentId": 123,
                                                        "contentType": "COACHING",
                                                        "title": "창업 아이디어 발굴 코칭",
                                                        "thumbnailUrl": "https://example.com/thumbnail1.jpg",
                                                        "sellerName": "창업멘토김",
                                                        "isContentScrap": true
                                                      },
                                                      {
                                                        "contentId": 456,
                                                        "contentType": "COACHING",
                                                        "title": "스타트업 투자 유치 전략",
                                                        "thumbnailUrl": "https://example.com/thumbnail2.jpg",
                                                        "sellerName": "벤처캐피탈박",
                                                        "isContentScrap": true
                                                      },
                                                      {
                                                        "contentId": 789,
                                                        "contentType": "COACHING",
                                                        "title": "비즈니스 모델 검증 워크숍",
                                                        "thumbnailUrl": "https://example.com/thumbnail3.jpg",
                                                        "sellerName": "창업코치이",
                                                        "isContentScrap": true
                                                      }
                                                    ],
                                                    "nextCursor": "789",
                                                    "hasNext": true,
                                                    "size": 3,
                                                    "meta": {
                                                      "filter": "COACHING",
                                                      "cursorType": "id"
                                                    }
                                                  },
                                                  "timestamp": "2025-05-08 14:26:26"
                                                }
                                                """),
                @ExampleObject(
                    name = "자료 콘텐츠 스크랩 목록",
                    summary = "자료 유형의 스크랩된 콘텐츠 목록",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "스크랩 콘텐츠 조회 성공",
                                                  "data": {
                                                    "items": [
                                                      {
                                                        "contentId": 234,
                                                        "contentType": "DOCUMENT",
                                                        "title": "IR 피치덱 템플릿 모음",
                                                        "thumbnailUrl": "https://example.com/doc_thumbnail1.jpg",
                                                        "sellerName": "투자전문가정",
                                                        "isContentScrap": true
                                                      },
                                                      {
                                                        "contentId": 567,
                                                        "contentType": "DOCUMENT",
                                                        "title": "스타트업 재무계획 엑셀 템플릿",
                                                        "thumbnailUrl": "https://example.com/doc_thumbnail2.jpg",
                                                        "sellerName": "스타트업CFO최",
                                                        "isContentScrap": true
                                                      },
                                                      {
                                                        "contentId": 890,
                                                        "contentType": "DOCUMENT",
                                                        "title": "창업지원사업 신청서 작성 가이드",
                                                        "thumbnailUrl": "https://example.com/doc_thumbnail3.jpg",
                                                        "sellerName": "창업컨설턴트윤",
                                                        "isContentScrap": true
                                                      }
                                                    ],
                                                    "nextCursor": "890",
                                                    "hasNext": false,
                                                    "size": 3,
                                                    "meta": {
                                                      "filter": "DOCUMENT",
                                                      "cursorType": "id"
                                                    }
                                                  },
                                                  "timestamp": "2025-05-08 14:26:26"
                                                }
                                                """),
                @ExampleObject(
                    name = "빈 스크랩 목록",
                    summary = "스크랩한 콘텐츠가 없는 경우",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "스크랩 콘텐츠 조회 성공",
                                                  "data": {
                                                    "items": [],
                                                    "nextCursor": null,
                                                    "hasNext": false,
                                                    "size": 0,
                                                    "meta": {
                                                      "filter": "COACHING",
                                                      "cursorType": "id"
                                                    }
                                                  },
                                                  "timestamp": "2025-05-08 14:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "500", description = "서버 내부 오류")
})
@Tag(name = "Scrap", description = "스크랩 관련 API")
@Parameter(
    name = "lastContentId",
    description = "마지막으로 조회한 콘텐츠 ID (첫 페이지는 null)",
    in = ParameterIn.QUERY,
    schema = @Schema(type = "integer"))
@Parameter(
    name = "size",
    description = "페이지 크기",
    in = ParameterIn.QUERY,
    schema = @Schema(type = "integer", defaultValue = "10"))
@Parameter(
    name = "contentType",
    description = "콘텐츠 유형 [COACHING - 코칭, DOCUMENT - 자료]",
    in = ParameterIn.QUERY,
    schema =
        @Schema(
            type = "string",
            allowableValues = {"COACHING", "DOCUMENT"}))
public @interface ContentScrapCard {}
