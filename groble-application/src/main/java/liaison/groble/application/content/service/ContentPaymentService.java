package liaison.groble.application.content.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.dto.ContentPayPageResponse;
import liaison.groble.application.coupon.service.CouponService;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentPaymentService {
  private final ContentReader contentReader;
  private final CouponService couponService;

  @Transactional(readOnly = true)
  public ContentPayPageResponse getContentPayPage(Long contentId, Long optionId, Long userId) {
    // 1. 콘텐츠 조회
    Content content = contentReader.getContentById(contentId);

    // 2. 콘텐츠 옵션 조회
    ContentOption contentOption = findContentOptionById(content, optionId);

    // 3. 사용자 쿠폰 조회 (인증된 사용자인 경우에만)
    List<ContentPayPageResponse.UserCouponResponse> userCoupons = null;
    if (userId != null) {
      userCoupons = couponService.getUserCoupons(userId);
    }

    // 4. 응답 DTO 생성
    return ContentPayPageResponse.builder()
        .thumbnailUrl(content.getThumbnailUrl())
        .sellerName(content.getUser().getNickname())
        .title(content.getTitle())
        .optionName(contentOption.getName())
        .price(contentOption.getPrice())
        .userCoupons(userCoupons)
        .build();
  }

  /** 기존 메서드 호환성을 위한 오버로드 (userId 없이 호출) */
  @Transactional(readOnly = true)
  public ContentPayPageResponse getContentPayPage(Long contentId, Long optionId) {
    return getContentPayPage(contentId, optionId, null);
  }

  /**
   * Content의 옵션 리스트에서 특정 optionId에 해당하는 ContentOption을 찾습니다.
   *
   * @param content 콘텐츠 엔티티
   * @param optionId 찾을 옵션 ID
   * @return 해당하는 ContentOption
   * @throws EntityNotFoundException 옵션을 찾을 수 없는 경우
   */
  private ContentOption findContentOptionById(Content content, Long optionId) {
    return content.getOptions().stream()
        .filter(option -> option.getId().equals(optionId))
        .findFirst()
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "콘텐츠 옵션을 찾을 수 없습니다. ContentId: "
                        + content.getId()
                        + ", OptionId: "
                        + optionId));
  }
}
