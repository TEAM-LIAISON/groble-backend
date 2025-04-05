package liaison.groble.application.user.service;

public interface UserService {

  /**
   * 사용자 역할 전환 (판매자/구매자 모드 전환)
   *
   * @param email 사용자 이메일
   * @param userType 전환할 유형 ("SELLER" 또는 "BUYER")
   * @return 전환 성공 여부
   */
  boolean switchUserType(String email, String userType);
}
