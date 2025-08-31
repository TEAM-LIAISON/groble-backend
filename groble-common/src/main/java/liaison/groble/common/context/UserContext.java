package liaison.groble.common.context;

/** 사용자 컨텍스트를 추상화하는 인터페이스 회원/비회원 구분 로직을 일원화합니다. */
public interface UserContext {

  /**
   * 사용자 ID 반환
   *
   * @return 사용자 ID
   */
  Long getId();

  /**
   * 사용자 타입 반환
   *
   * @return "MEMBER" 또는 "GUEST"
   */
  String getUserType();

  /**
   * 회원인지 확인
   *
   * @return 회원이면 true
   */
  boolean isMember();

  /**
   * 비회원(게스트)인지 확인
   *
   * @return 비회원이면 true
   */
  boolean isGuest();

  /**
   * 인증된 사용자인지 확인
   *
   * @return 인증된 사용자이면 true
   */
  boolean isAuthenticated();
}
