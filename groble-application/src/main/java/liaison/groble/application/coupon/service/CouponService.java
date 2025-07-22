package liaison.groble.application.coupon.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import liaison.groble.application.content.dto.ContentPayPageDTO;
import liaison.groble.application.coupon.dto.UserCouponResponseDTO;
import liaison.groble.domain.coupon.dto.FlatUserCouponCardDTO;
import liaison.groble.domain.coupon.repository.UserCouponCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
  private final UserCouponCustomRepository userCouponCustomRepository;

  public List<UserCouponResponseDTO> getMyPageUserCoupons(Long userId) {
    List<FlatUserCouponCardDTO> flatUserCouponCardDTOS =
        userCouponCustomRepository.findAllUsableCouponsByUserId(userId);

    return flatUserCouponCardDTOS.stream()
        .map(this::convertFlatDTOToCouponDTO)
        .collect(Collectors.toList());
  }

  public List<ContentPayPageDTO.UserCouponDTO> getUserCoupons(Long userId) {
    // 내가 사용 가능한 쿠폰들 조회 [쿠폰 상태가 발급됨, 유효 기간이 남아있는 쿠폰들 조회, 쿠폰 활성화 되어야 함]
    List<FlatUserCouponCardDTO> flatUserCouponCardDTOs =
        userCouponCustomRepository.findAllUsableCouponsByUserId(userId);

    return flatUserCouponCardDTOs.stream()
        .map(this::convertFlatDTOToUserCouponDTO)
        .collect(Collectors.toList());
  }

  private ContentPayPageDTO.UserCouponDTO convertFlatDTOToUserCouponDTO(
      FlatUserCouponCardDTO flat) {
    return ContentPayPageDTO.UserCouponDTO.builder()
        .couponCode(flat.getCouponCode())
        .name(flat.getTitle())
        .couponType(flat.getCouponType())
        .discountValue(flat.getDiscountValue())
        .validUntil(flat.getValidUntil())
        .minOrderPrice(flat.getMinOrderPrice())
        .build();
  }

  private UserCouponResponseDTO convertFlatDTOToCouponDTO(FlatUserCouponCardDTO flat) {
    return UserCouponResponseDTO.builder()
        .couponCode(flat.getCouponCode())
        .name(flat.getTitle())
        .couponType(flat.getCouponType())
        .discountValue(flat.getDiscountValue())
        .validUntil(flat.getValidUntil())
        .minOrderPrice(flat.getMinOrderPrice())
        .build();
  }
}
