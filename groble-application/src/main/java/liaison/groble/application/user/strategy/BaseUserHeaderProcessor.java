package liaison.groble.application.user.strategy;

import jakarta.servlet.http.HttpServletResponse;

import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.common.context.UserContext;

/**
 * 사용자 헤더 처리기의 공통 기능을 제공하는 추상 클래스
 *
 * <p>Template Method 패턴과 Strategy 패턴을 결합하여 공통 로직은 상위 클래스에서 처리하고, 구체적인 처리는 하위 클래스에서 구현합니다. Order
 * 패키지의 BaseOrderProcessor와 동일한 구조로 설계되었습니다.
 */
public abstract class BaseUserHeaderProcessor implements UserHeaderStrategy {

  @Override
  public final UserHeaderDTO processUserHeader(
      UserContext userContext, HttpServletResponse httpResponse) {
    // Template Method 패턴: 공통 흐름 정의
    if (isMemberContext(userContext)) {
      return createMemberResponse(userContext, httpResponse);
    } else if (isAuthenticatedGuestContext(userContext)) {
      return createAuthenticatedGuestResponse(userContext, httpResponse);
    } else {
      return createAnonymousGuestResponse(httpResponse);
    }
  }

  /**
   * 사용자 컨텍스트가 유효한 회원인지 확인합니다.
   *
   * @param userContext 사용자 컨텍스트
   * @return 유효한 회원 여부
   */
  protected abstract boolean isMemberContext(UserContext userContext);

  /**
   * 사용자 컨텍스트가 토큰으로 식별된 게스트인지 확인합니다.
   *
   * @param userContext 사용자 컨텍스트
   * @return 인증된 게스트 여부
   */
  protected abstract boolean isAuthenticatedGuestContext(UserContext userContext);

  /**
   * 회원용 응답을 생성합니다.
   *
   * @param userContext 사용자 컨텍스트
   * @param httpResponse HTTP 응답 객체
   * @return 회원용 헤더 응답
   */
  protected abstract UserHeaderDTO createMemberResponse(
      UserContext userContext, HttpServletResponse httpResponse);

  /**
   * 토큰으로 식별된 게스트용 응답을 생성합니다.
   *
   * @param userContext 사용자 컨텍스트
   * @param httpResponse HTTP 응답 객체
   * @return 인증된 게스트용 헤더 응답
   */
  protected abstract UserHeaderDTO createAuthenticatedGuestResponse(
      UserContext userContext, HttpServletResponse httpResponse);

  /**
   * 완전한 익명 사용자(토큰 없음)용 기본 응답을 생성합니다. 공통 로직으로 제공하되, 필요시 하위 클래스에서 오버라이드 가능합니다.
   *
   * @param httpResponse HTTP 응답 객체
   * @return 익명 사용자용 헤더 응답
   */
  protected UserHeaderDTO createAnonymousGuestResponse(HttpServletResponse httpResponse) {
    return UserHeaderDTO.builder()
        .isLogin(false)
        .nickname(null)
        .email(null)
        .profileImageUrl(null)
        .canSwitchToSeller(false)
        .unreadNotificationCount(0)
        .alreadyRegisteredAsSeller(false)
        .lastUserType(null)
        .build();
  }
}
