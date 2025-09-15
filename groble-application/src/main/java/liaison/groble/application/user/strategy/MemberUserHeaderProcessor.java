package liaison.groble.application.user.strategy;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.utils.TokenCookieService;

/** 로그인한 회원의 헤더 정보 처리 구현체 */
@Component
public class MemberUserHeaderProcessor extends BaseUserHeaderProcessor {

  private final UserService userService;
  private final TokenCookieService tokenCookieService;

  public MemberUserHeaderProcessor(UserService userService, TokenCookieService tokenCookieService) {
    this.userService = userService;
    this.tokenCookieService = tokenCookieService;
  }

  @Override
  public String getSupportedUserType() {
    return "MEMBER";
  }

  @Override
  protected boolean isMemberContext(UserContext userContext) {
    return userContext.isMember() && userService.isLoginAble(userContext.getId());
  }

  @Override
  protected boolean isAuthenticatedGuestContext(UserContext userContext) {
    // 회원 처리기에서는 게스트를 처리하지 않음
    return false;
  }

  @Override
  protected UserHeaderDTO createMemberResponse(
      UserContext userContext, HttpServletResponse httpResponse) {
    // 로그인한 회원의 헤더 정보 조회
    return userService.getUserHeaderInform(userContext.getId());
  }

  @Override
  protected UserHeaderDTO createAuthenticatedGuestResponse(
      UserContext userContext, HttpServletResponse httpResponse) {
    // 회원 처리기에서는 게스트 응답을 생성하지 않음
    // 안전성을 위해 익명 게스트 응답 반환
    return createAnonymousGuestResponse(httpResponse);
  }

  @Override
  protected UserHeaderDTO createAnonymousGuestResponse(HttpServletResponse httpResponse) {
    // 토큰이 유효하지 않은 경우 로그아웃 처리
    tokenCookieService.removeTokenCookies(httpResponse);

    // 부모 클래스의 기본 구현 사용
    return super.createAnonymousGuestResponse(httpResponse);
  }
}
