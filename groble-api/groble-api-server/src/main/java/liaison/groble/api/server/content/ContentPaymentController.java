package liaison.groble.api.server.content;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.content.dto.ContentPayPageResponse;
import liaison.groble.application.content.service.ContentPaymentService;
import liaison.groble.application.coupon.service.CouponService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "콘텐츠 결제 관련 API", description = "콘텐츠 결제창 정보 조회")
public class ContentPaymentController {

  private final ContentPaymentService contentPaymentService;
  private final CouponService couponService;

  @Operation(
      summary = "콘텐츠 결제 페이지 조회",
      description = "콘텐츠 결제 페이지 렌더링에 필요한 정보를 조회합니다",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                """
                    {
                      "success": true,
                      "data": {
                        "thumbnailUrl": "https://example.com/thumbnail.jpg",
                        "sellerName": "홍길동",
                        "title": "프로그래밍 입문 과정",
                        "contentType": "COACHING",
                        "optionName": "1:1 개인 코칭 (1개월)",
                        "price": 150000,
                        "userCoupons": [
                          {
                            "couponCode": "WELCOME10",
                            "name": "신규 가입 쿠폰",
                            "couponType": "PERCENTAGE",
                            "discountValue": 10,
                            "validUntil": "2024-12-31T23:59:59",
                            "minOrderPrice": 50000
                          }
                        ]
                      }
                    }
                    """)))
      })
  @GetMapping("/content/{contentId}/pay/{optionId}")
  public ResponseEntity<GrobleResponse<ContentPayPageResponse>> getContentPayPage(
      @Auth(required = false) Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @PathVariable("optionId") Long optionId) {
    // 기본 컨텐츠 결제 페이지 정보 조회
    ContentPayPageResponse baseResponse =
        contentPaymentService.getContentPayPage(contentId, optionId);

    // 사용자 쿠폰 정보 조회 (인증된 사용자만)
    List<ContentPayPageResponse.UserCouponResponse> userCoupons =
        accessor.isAuthenticated() ? couponService.getUserCoupons(accessor.getUserId()) : null;

    // 쿠폰 정보를 포함한 최종 응답 객체 생성
    ContentPayPageResponse contentPayPageResponse =
        ContentPayPageResponse.builder()
            .thumbnailUrl(baseResponse.getThumbnailUrl())
            .sellerName(baseResponse.getSellerName())
            .title(baseResponse.getTitle())
            .contentType(baseResponse.getContentType())
            .optionName(baseResponse.getOptionName())
            .price(baseResponse.getPrice())
            .userCoupons(userCoupons)
            .build();

    // 성공 응답 반환
    return ResponseEntity.ok(GrobleResponse.success(contentPayPageResponse));
  }
}
