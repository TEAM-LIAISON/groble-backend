package liaison.groble.application.user.service;

import liaison.groble.application.user.dto.UserHeaderDto;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;

public interface UserService {

  /**
   * 사용자 유형 조회 "SELLER", "BUYER", "NONE" 등의 값을 반환
   *
   * @param email 사용자 이메일
   * @return 사용자 유형
   */
  String getUserType(String email);

  /**
   * 사용자 역할 전환
   *
   * @param userId 사용자 ID
   * @param userTypeString 전환할 유형 ("SELLER", "BUYER")
   * @return 전환 성공 여부
   */
  boolean switchUserType(Long userId, String userTypeString);

  /**
   * 사용자 로그인 이후 라우팅 경로 조회 "/home", "/select/type" 등의 값을 반환
   *
   * @param email 사용자 이메일
   * @return 라우팅 경로
   */
  String getNextRoutePath(String email);

  /**
   * 비밀번호 생성/수정
   *
   * @param userId 사용자 ID
   * @param password 설정할 비밀번호
   */
  void setOrUpdatePassword(Long userId, String password);

  /**
   * 비밀번호 재설정
   *
   * @param token 비밀번호 재설정 토큰
   * @param newPassword 새로운 비밀번호
   */
  void resetPasswordWithToken(String token, String newPassword);

  /**
   * 사용자 마이페이지 정보 조회
   *
   * @param userId 사용자 ID
   * @return UserMyPageSummaryDto 사용자 마이페이지 정보
   */
  UserMyPageSummaryDto getUserMyPageSummary(Long userId);

  /**
   * 사용자 마이페이지 상세 정보 조회
   *
   * @param userId 사용자 ID
   * @return UserMyPageDetailDto 사용자 마이페이지 상세 정보
   */
  UserMyPageDetailDto getUserMyPageDetail(Long userId);

  /**
   * 사용자 초기 역할 설정
   *
   * @param userId 사용자 ID
   * @param userTypeName 설정할 가입 유형 이름
   */
  void setInitialUserType(Long userId, String userTypeName);

  /**
   * 사용자 헤더 정보 조회
   *
   * @return UserHeaderDto 사용자 헤더 정보
   */
  UserHeaderDto getUserHeaderInform(Long userId);

  void updateProfileImageUrl(Long userId, String profileImagePath);

  boolean isLoginAble(Long userId);
}
