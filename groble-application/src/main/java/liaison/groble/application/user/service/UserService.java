package liaison.groble.application.user.service;

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
  boolean switchUserType(Long userId, String userType);

  /**
   * 사용자 로그인 이후 라우팅 경로 조회 "/home", "/select/type" 등의 값을 반환
   *
   * @param email 사용자 이메일
   * @return 라우팅 경로
   */
  String getNextRoutePath(String email);
}
