package liaison.groble.application.user.service;

import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.domain.user.enums.UserType;

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
   * @param userType 전환할 유형 (SELLER 또는 BUYER)
   * @return 전환 성공 여부
   */
  boolean switchUserType(Long userId, UserType userType);

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
   * 닉네임 중복 확인
   *
   * @param nickName 확인할 닉네임
   * @return 중복 여부
   */
  boolean isNickNameTaken(String nickName);

  /**
   * 닉네임 설정 또는 업데이트
   *
   * @param nickName 설정할 닉네임
   * @return 설정된 닉네임
   */
  String setOrUpdateNickname(Long userId, String nickName);

  /**
   * 비밀번호 재설정 토큰 발송
   *
   * @param email 비밀번호를 재설정할 이메일
   */
  void sendPasswordResetToken(String email);

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
}
