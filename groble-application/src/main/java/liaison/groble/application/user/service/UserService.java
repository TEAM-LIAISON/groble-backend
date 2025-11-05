package liaison.groble.application.user.service;

import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.application.user.dto.UserMyPageDetailDTO;
import liaison.groble.application.user.dto.UserMyPageSummaryDTO;
import liaison.groble.application.user.dto.UserPaymentMethodDTO;

public interface UserService {

  /**
   * 사용자 역할 전환
   *
   * @param userId 사용자 ID
   * @param userTypeString 전환할 유형 ("SELLER", "BUYER")
   * @return 전환 성공 여부
   */
  boolean switchUserType(Long userId, String userTypeString);

  /**
   * 사용자 마이페이지 정보 조회
   *
   * @param userId 사용자 ID
   * @return UserMyPageSummaryDTO 사용자 마이페이지 정보
   */
  UserMyPageSummaryDTO getUserMyPageSummary(Long userId);

  /**
   * 사용자 마이페이지 상세 정보 조회
   *
   * @param userId 사용자 ID
   * @return UserMyPageDetailDTO 사용자 마이페이지 상세 정보
   */
  UserMyPageDetailDTO getUserMyPageDetail(Long userId);

  /**
   * 사용자 마이페이지 결제 수단 조회
   *
   * @param userId 사용자 ID
   * @return UserPaymentMethodDTO 결제 수단 정보
   */
  UserPaymentMethodDTO getUserPaymentMethod(Long userId);

  /**
   * 사용자 헤더 정보 조회
   *
   * @return UserHeaderDTO 사용자 헤더 정보
   */
  UserHeaderDTO getUserHeaderInform(Long userId);

  void updateProfileImageUrl(Long userId, String profileImagePath);

  boolean isLoginAble(Long userId);

  boolean isAllowWithdraw(Long userId);
}
