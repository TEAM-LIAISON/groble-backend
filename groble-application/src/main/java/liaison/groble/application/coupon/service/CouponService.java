package liaison.groble.application.coupon.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import liaison.groble.application.coupon.dto.UserCouponResponseDto;
import liaison.groble.domain.coupon.dto.FlatUserCouponCardDTO;
import liaison.groble.domain.coupon.repository.UserCouponCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
  private final UserCouponCustomRepository userCouponCustomRepository;

  public List<UserCouponResponseDto> getUserCoupons(Long userId) {
    // 내가 사용 가능한 쿠폰들 조회 [쿠폰 상태가 발급됨, 유효 기간이 남아있는 쿠폰들 조회, 쿠폰 활성화 되어야 함]
    List<FlatUserCouponCardDTO> flatUserCouponCardDTOs =
        userCouponCustomRepository.findAllUsableCouponsByUserId(userId);

    return flatUserCouponCardDTOs.stream()
        .map(this::convertFlatDtoToCouponDto)
        .collect(Collectors.toList());
  }

  /** FlatPreviewContentDTO를 ContentCardDto로 변환합니다. */
  private UserCouponResponseDto convertFlatDtoToCouponDto(FlatUserCouponCardDTO flat) {
    return UserCouponResponseDto.builder()
        .couponCode(flat.getCouponCode())
        .name(flat.getCouponCode())
        .couponType(flat.getCouponType())
        .discountValue(flat.getDiscountValue())
        .validUntil(flat.getValidUntil())
        .minOrderPrice(flat.getMinOrderPrice())
        .build();
  }
}
