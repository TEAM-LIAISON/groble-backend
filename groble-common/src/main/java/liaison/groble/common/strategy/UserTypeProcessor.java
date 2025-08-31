package liaison.groble.common.strategy;

/**
 * 사용자 타입별 처리 전략의 공통 인터페이스
 *
 * <p>Strategy 패턴에서 모든 프로세서가 구현해야 하는 기본 메소드를 정의합니다. 각 도메인의 처리 전략들이 이 인터페이스를 확장하여 일관된 팩토리 패턴을 사용할 수
 * 있게 합니다.
 */
public interface UserTypeProcessor {

  /**
   * 지원하는 사용자 타입을 반환합니다.
   *
   * @return 사용자 타입 ("MEMBER" 또는 "GUEST")
   */
  String getSupportedUserType();
}
