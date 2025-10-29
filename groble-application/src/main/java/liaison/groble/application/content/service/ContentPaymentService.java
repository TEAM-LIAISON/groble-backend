package liaison.groble.application.content.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.dto.ContentPayPageDTO;
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
  // Reader
  private final ContentReader contentReader;

  // Service
  private final CouponService couponService;

  @Transactional(readOnly = true)
  public ContentPayPageDTO getContentPayPage(Long userId, Long contentId, Long optionId) {
    // 1. 콘텐츠 조회
    Content content = contentReader.getContentById(contentId);

    // 2. 콘텐츠 옵션 조회
    ContentOption contentOption = findContentOptionById(content, optionId);

    boolean isLoggedIn = userId != null;
    List<ContentPayPageDTO.UserCouponDTO> userCoupons =
        isLoggedIn ? couponService.getUserCoupons(userId) : Collections.emptyList();

    // 3. 응답 DTO 생성
    return buildContentPayPageDTO(isLoggedIn, content, contentOption, userCoupons);
  }

  private ContentPayPageDTO buildContentPayPageDTO(
      boolean isLoggedIn,
      Content content,
      ContentOption contentOption,
      List<ContentPayPageDTO.UserCouponDTO> userCoupons) {
    return ContentPayPageDTO.builder()
        .isLoggedIn(isLoggedIn)
        .thumbnailUrl(content.getThumbnailUrl())
        .sellerName(content.getUser().getNickname())
        .title(content.getTitle())
        .contentType(content.getContentType().name())
        .paymentType(content.getPaymentType() != null ? content.getPaymentType().name() : null)
        .optionName(contentOption.getName())
        .price(contentOption.getPrice())
        .userCoupons(userCoupons)
        .build();
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
