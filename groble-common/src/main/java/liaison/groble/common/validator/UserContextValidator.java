package liaison.groble.common.validator;

import liaison.groble.common.context.UserContext;

/** 사용자 컨텍스트 검증 유틸리티 공통적인 사용자 타입 검증 로직을 제공합니다. */
public final class UserContextValidator {

  private UserContextValidator() {
    // 유틸리티 클래스는 인스턴스 생성 방지
  }

  /**
   * 인증된 사용자인지 검증
   *
   * @param userContext 사용자 컨텍스트
   * @throws IllegalArgumentException 인증되지 않은 경우
   */
  public static void validateAuthenticated(UserContext userContext) {
    if (!userContext.isAuthenticated()) {
      throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
    }
  }

  /**
   * 회원 사용자인지 검증
   *
   * @param userContext 사용자 컨텍스트
   * @throws IllegalArgumentException 회원이 아닌 경우
   */
  public static void validateMember(UserContext userContext) {
    validateAuthenticated(userContext);
    if (!userContext.isMember()) {
      throw new IllegalArgumentException("회원 사용자만 접근 가능합니다.");
    }
  }

  /**
   * 비회원(게스트) 사용자인지 검증
   *
   * @param userContext 사용자 컨텍스트
   * @throws IllegalArgumentException 비회원이 아닌 경우
   */
  public static void validateGuest(UserContext userContext) {
    validateAuthenticated(userContext);
    if (!userContext.isGuest()) {
      throw new IllegalArgumentException("비회원 사용자만 접근 가능합니다.");
    }
  }

  /**
   * 특정 사용자 타입인지 검증
   *
   * @param userContext 사용자 컨텍스트
   * @param expectedType 기대하는 사용자 타입 ("MEMBER" 또는 "GUEST")
   * @throws IllegalArgumentException 타입이 일치하지 않는 경우
   */
  public static void validateUserType(UserContext userContext, String expectedType) {
    validateAuthenticated(userContext);
    if (!expectedType.equals(userContext.getUserType())) {
      throw new IllegalArgumentException(
          String.format(
              "사용자 타입이 일치하지 않습니다. 예상: %s, 실제: %s", expectedType, userContext.getUserType()));
    }
  }

  /**
   * 사용자 ID가 null이 아닌지 검증
   *
   * @param userContext 사용자 컨텍스트
   * @throws IllegalArgumentException 사용자 ID가 null인 경우
   */
  public static void validateUserId(UserContext userContext) {
    if (userContext.getId() == null) {
      throw new IllegalArgumentException("사용자 ID가 null입니다.");
    }
  }
}
