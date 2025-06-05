package liaison.groble.application.content.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

/**
 * 콘텐츠 결제 페이지 렌더링용 응답 DTO
 *
 * <p>이 DTO는 콘텐츠 결제 페이지를 렌더링하기 위한 정보를 포함합니다. 클라이언트가 결제 페이지를 표시할 때 필요한 모든 정보를 제공합니다.
 */
@Getter
@Builder
public class ContentPayPageResponse {
  // 사용자가 로그인했는지 여부
  private final boolean isLoggedIn;
  // 콘텐츠 썸네일 URL
  private final String thumbnailUrl;
  // 판매자 이름
  private final String sellerName;
  // 콘텐츠 이름
  private final String title;
  // 콘텐츠 유형
  private final String contentType;
  // 옵션 이름
  private final String optionName;
  // 옵션에 속하는 가격
  private final BigDecimal price;
  // 사용자가 보유한 쿠폰 목록
  private final List<UserCouponResponse> userCoupons;

  /**
   * 사용자 쿠폰 응답 DTO
   *
   * <p>이 DTO는 사용자가 보유한 쿠폰 정보를 포함합니다. 클라이언트는 이 정보를 기반으로 쿠폰 선택 UI를 렌더링합니다.
   */
  @Getter
  @Builder
  public static class UserCouponResponse {
    private final String couponCode;
    private final String name;
    private final String couponType;
    private final BigDecimal discountValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime validUntil;

    private final BigDecimal minOrderPrice;
  }
}
