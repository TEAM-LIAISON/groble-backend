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
@Operation(summary = "콘텐츠 임시 저장", description = "콘텐츠를 임시 저장합니다. 콘텐츠 유형(코칭/문서)에 따라 옵션 구조가 달라집니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "콘텐츠 임시 저장 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentDraftApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "코칭 콘텐츠 임시 저장 응답",
                    summary = "코칭 유형 콘텐츠 임시 저장 성공",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "콘텐츠 임시 저장 성공",
                                                  "data": {
                                                    "id": 1,
                                                    "title": "사업계획서 컨설팅",
                                                    "contentType": "COACHING",
                                                    "categoryId": 1,
                                                    "categoryName": "비즈니스 컨설팅",
                                                    "thumbnailUrl": "https://example.com/thumbnail.jpg",
                                                    "status": "DRAFT",
                                                    "options": [
                                                      {
                                                        "id": 1,
                                                        "name": "스탠다드 코칭",
                                                        "description": "기본적인 컨설팅을 제공합니다.",
                                                        "price": 50000,
                                                        "coachingPeriod": "ONE_WEEK",
                                                        "documentProvision": "PROVIDED",
                                                        "coachingType": "ONLINE",
                                                        "coachingTypeDescription": "줌을 통한 온라인 미팅으로 진행됩니다."
                                                      }
                                                    ]
                                                  },
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """),
                @ExampleObject(
                    name = "자료 콘텐츠 임시 저장 응답",
                    summary = "자료 유형 콘텐츠 임시 저장 성공",
                    value =
                        """
                                                {
                                                  "status": "SUCCESS",
                                                  "code": 200,
                                                  "message": "콘텐츠 임시 저장 성공",
                                                  "data": {
                                                    "id": 2,
                                                    "title": "표준 사업계획서 템플릿",
                                                    "contentType": "DOCUMENT",
                                                    "categoryId": 2,
                                                    "categoryName": "사업계획서",
                                                    "thumbnailUrl": "https://example.com/document-thumbnail.jpg",
                                                    "status": "DRAFT",
                                                    "options": [
                                                      {
                                                        "id": 2,
                                                        "name": "표준 자료 패키지",
                                                        "description": "사업계획서 작성에 필요한 모든 템플릿이 포함되어 있습니다.",
                                                        "price": 30000,
                                                        "contentDeliveryMethod": "IMMEDIATE_DOWNLOAD"
                                                      }
                                                    ]
                                                  },
                                                  "timestamp": "2025-05-06 04:26:26"
                                                }
                                                """)
              })),
  @ApiResponse(responseCode = "400", description = "잘못된 요청"),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "403", description = "권한 없음")
})
public @interface ContentDraft {}
