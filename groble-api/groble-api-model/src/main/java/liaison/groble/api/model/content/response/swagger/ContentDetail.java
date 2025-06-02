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
@Operation(summary = "콘텐츠 상세 정보 조회", description = "콘텐츠 상세 정보를 조회합니다. [코칭 & 자료]")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "콘텐츠 상세 정보 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentDetailApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "코칭 콘텐츠 상세 정보",
                    summary = "코칭 유형 콘텐츠 상세 정보",
                    value =
                        """
                                                {
                                                  "status":  "SUCCESS",
                                                  "code":    200,
                                                  "message": "콘텐츠 상세 조회 성공",
                                                  "data": {
                                                    "contentId":               1,
                                                    "status":                  "ACTIVE",
                                                    "thumbnailUrl":            "https://example.com/thumbnail1.jpg",
                                                    "contentType":             "COACHING",
                                                    "categoryId":              1,
                                                    "title":                   "사업계획서 컨설팅",
                                                    "sellerProfileImageUrl":   "https://example.com/profile.jpg",
                                                    "sellerName":              "전문가",
                                                    "lowestPrice":             50000,
                                                    "options": [
                                                      {
                                                        "optionId":                  1,
                                                        "optionType":                "COACHING_OPTION",
                                                        "name":                      "1시간 코칭",
                                                        "description":               "1:1 전문가 코칭 1시간",
                                                        "price":                     50000,
                                                        "coachingPeriod":            "ONE_DAY",
                                                        "documentProvision":         "PROVIDED",
                                                        "coachingType":              "ONLINE",
                                                        "coachingTypeDescription":   "줌을 통한 온라인 미팅으로 진행됩니다.",
                                                        "contentDeliveryMethod":     null
                                                      },
                                                      {
                                                        "optionId":                  2,
                                                        "optionType":                "COACHING_OPTION",
                                                        "name":                      "2시간 코칭",
                                                        "description":               "1:1 전문가 코칭 2시간",
                                                        "price":                     90000,
                                                        "coachingPeriod":            "ONE_DAY",
                                                        "documentProvision":         "PROVIDED",
                                                        "coachingType":              "ONLINE",
                                                        "coachingTypeDescription":   "줌을 통한 온라인 미팅으로 진행됩니다.",
                                                        "contentDeliveryMethod":     null
                                                      }
                                                    ],
                                                    "contentIntroduction":     "전문가와 함께하는 1:1 사업계획서 컨설팅 서비스입니다.",
                                                    "serviceTarget":           "초창패, 창중, 예창패 등에 도전하는 예비 창업자",
                                                    "serviceProcess":          "1단계: 상담 → 2단계: 기획서 작성 → 3단계: 피드백",
                                                    "makerIntro":              "- 동국대학교 철학과 졸업\\n- 前 스타트업 창업 5년 경력\\n- 전문 사업계획서 컨설턴트"
                                                  },
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """),
                @ExampleObject(
                    name = "문서 콘텐츠 상세 정보",
                    summary = "문서 유형 콘텐츠 상세 정보",
                    value =
                        """
                                                {
                                                  "status":  "SUCCESS",
                                                  "code":    200,
                                                  "message": "콘텐츠 상세 조회 성공",
                                                  "data": {
                                                    "contentId":               2,
                                                    "status":                  "DRAFT",
                                                    "thumbnailUrl":            "https://example.com/thumbnail2.jpg",
                                                    "contentType":             "DOCUMENT",
                                                    "categoryId":              2,
                                                    "title":                   "사업계획서 템플릿 모음",
                                                    "sellerProfileImageUrl":   "https://example.com/profile2.jpg",
                                                    "sellerName":              "자료제공자",
                                                    "lowestPrice":             15000,
                                                    "options": [
                                                      {
                                                        "optionId":                  3,
                                                        "optionType":                "DOCUMENT_OPTION",
                                                        "name":                      "기본 템플릿",
                                                        "description":               "기본적인 사업계획서 템플릿입니다.",
                                                        "price":                     15000,
                                                        "coachingPeriod":            null,
                                                        "documentProvision":         null,
                                                        "coachingType":              null,
                                                        "coachingTypeDescription":   null,
                                                        "contentDeliveryMethod":     "IMMEDIATE_DOWNLOAD"
                                                      },
                                                      {
                                                        "optionId":                  4,
                                                        "optionType":                "DOCUMENT_OPTION",
                                                        "name":                      "프리미엄 템플릿",
                                                        "description":               "상세한 작성 가이드가 포함된 프리미엄 템플릿입니다.",
                                                        "price":                     25000,
                                                        "coachingPeriod":            null,
                                                        "documentProvision":         null,
                                                        "coachingType":              null,
                                                        "coachingTypeDescription":   null,
                                                        "contentDeliveryMethod":     "IMMEDIATE_DOWNLOAD"
                                                      }
                                                    ],
                                                    "contentIntroduction":     "다양한 사업계획서 템플릿이 포함된 패키지입니다.",
                                                    "serviceTarget":           "스타트업 설립 단계의 예비 창업자",
                                                    "serviceProcess":          "템플릿 다운로드 후 작성",
                                                    "makerIntro":              "- 스타트업 액셀러레이터 5년 근무\\n- 200개 이상의 사업계획서 검토 경험"
                                                  },
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "콘텐츠 정보를 찾을 수 없음")
})
public @interface ContentDetail {}
