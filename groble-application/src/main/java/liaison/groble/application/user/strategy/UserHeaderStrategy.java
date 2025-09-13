package liaison.groble.application.user.strategy;

import jakarta.servlet.http.HttpServletResponse;

import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.strategy.UserTypeProcessor;

/**
 * 사용자 헤더 정보 처리를 위한 Strategy 인터페이스
 *
 * <p>회원/비회원에 따라 다른 헤더 정보 처리 로직을 구현합니다. Order 패키지의 OrderProcessorStrategy와 동일한 구조로 설계되었습니다.
 */
public interface UserHeaderStrategy extends UserTypeProcessor {

  UserHeaderDTO processUserHeader(UserContext userContext, HttpServletResponse httpResponse);
}
