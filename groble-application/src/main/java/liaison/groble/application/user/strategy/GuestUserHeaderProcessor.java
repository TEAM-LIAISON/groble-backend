package liaison.groble.application.user.strategy;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.common.context.UserContext;

/** 비회원(게스트)의 헤더 정보 처리 구현체 */
@Component
public class GuestUserHeaderProcessor extends BaseUserHeaderProcessor {

  @Override
  public String getSupportedUserType() {
    return "GUEST";
  }

  @Override
  protected boolean isValidUserContext(UserContext userContext) {
    // 비회원은 항상 유효하지 않은 컨텍스트로 간주
    return false;
  }

  @Override
  protected UserHeaderDTO createMemberResponse(
      UserContext userContext, HttpServletResponse httpResponse) {
    // 비회원은 회원 응답을 생성하지 않음
    // 이 메서드가 호출되지 않아야 하지만, 안전성을 위해 게스트 응답 반환
    return createGuestResponse(httpResponse);
  }
}
