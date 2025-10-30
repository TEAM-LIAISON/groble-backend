package liaison.groble.api.model.content.response.pay;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import liaison.groble.api.model.coupon.response.UserCouponResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "콘텐츠 결제 페이지 응답")
public class ContentPayPageResponse {
  @Schema(
      description = "로그인 상태 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean isLoggedIn;

  @Schema(
      description = "콘텐츠 썸네일 이미지 URL",
      example = "https://example.com/thumbnail.jpg",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String thumbnailUrl;

  @Schema(
      description = "콘텐츠 판매자 이름",
      example = "홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String sellerName;

  @Schema(
      description = "콘텐츠 제목",
      example = "프로그래밍 입문 과정",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @Schema(
      description = "콘텐츠 유형",
      example = "COACHING",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentType;

  @Schema(
      description = "결제 유형",
      example = "ONE_TIME",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String paymentType;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @Schema(description = "다음 결제 예정일 (정기 결제 시 제공)", example = "2024-11-15", type = "string")
  private LocalDate nextPaymentDate;

  @Schema(
      description = "옵션 이름",
      example = "기본 옵션",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String optionName;

  @Schema(
      description = "옵션 가격",
      example = "10000.00",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal price;

  @Schema(
      description = "사용자가 보유한 쿠폰 목록",
      implementation = UserCouponResponse.class,
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<UserCouponResponse> userCoupons;
}
