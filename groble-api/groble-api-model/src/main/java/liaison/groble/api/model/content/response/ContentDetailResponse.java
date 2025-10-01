package liaison.groble.api.model.content.response;

import java.math.BigDecimal;
import java.util.List;

import liaison.groble.api.model.maker.response.ContactInfoResponse;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 상세 정보 응답")
public class ContentDetailResponse {
  @Schema(description = "콘텐츠 ID", example = "1")
  private Long contentId;

  @Schema(
      description = "콘텐츠 상태 [ACTIVE - 판매중], [DRAFT - 작성중], [DELETED - 삭제됨], [DISCONTINUED - 판매중단]",
      example = "DRAFT",
      allowableValues = {"ACTIVE", "DRAFT", "DELETED", "DISCONTINUED"})
  private String status;

  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail1.jpg")
  private String thumbnailUrl;

  @Schema(description = "콘텐츠 유형 [COACHING - 코칭], [DOCUMENT - 자료]", example = "COACHING")
  private String contentType;

  @Schema(description = "카테고리 ID", example = "1")
  private String categoryId;

  @Schema(description = "콘텐츠 이름", example = "사업계획서 컨설팅")
  private String title;

  @Schema(description = "검색 엔진 노출 여부", example = "true")
  private Boolean isSearchExposed;

  @Schema(description = "판매자 프로필 이미지 URL")
  private String sellerProfileImageUrl;

  @Schema(description = "판매자 이름")
  private String sellerName;

  @Schema(description = "콘텐츠 최저가", example = "10000")
  private BigDecimal lowestPrice;

  @Schema(description = "가격 옵션 개수", example = "3")
  private int priceOptionLength;

  @ArraySchema(
      schema =
          @Schema(
              implementation = OptionResponseDoc.class, // ← 문서용 DTO
              description = "콘텐츠 옵션 목록",
              example =
                  """
      [
        {
          "optionId": 1,
          "optionType": "COACHING_OPTION",
          "name": "1시간 코칭",
          "description": "1:1 전문가 코칭 1시간",
          "price": 50000
        },
        {
          "optionId": 3,
          "optionType": "DOCUMENT_OPTION",
          "name": "기본 템플릿",
          "description": "기본적인 사업계획서 템플릿",
          "price": 15000,
          "documentFileUrl": "https://example.com/template.pdf",
          "documentLinkUrl": "https://example.com/template-link",
          "documentOriginalFileName": "template.pdf"
        }
      ]
      """))
  private List<?> options; // 실제론 BaseOptionResponse 계열이지만, 문서에선 와일드카드로 처리

  @Schema(description = "콘텐츠 소개")
  private String contentIntroduction;

  @Schema(description = "서비스 타겟", example = "초창패, 창중, 예창패, 청창사 등을 준비하는 분")
  private String serviceTarget;

  @Schema(description = "제공 절차", example = "STANDARD/DELUXE/PREMIUM")
  private String serviceProcess;

  @Schema(description = "메이커 소개", example = "- 동국대학교 철학과 졸업")
  private String makerIntro;

  @Schema(
      description = "문의하기 응답 객체",
      example = "https://example.com/contact",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private ContactInfoResponse contactInfo;
}
