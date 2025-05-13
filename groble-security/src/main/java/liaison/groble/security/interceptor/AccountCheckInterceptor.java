package liaison.groble.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import liaison.groble.common.annotation.RequireAccount;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.security.exception.UnauthorizedException;
import liaison.groble.security.jwt.UserDetailsImpl;

@Component
public class AccountCheckInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    // 메서드 또는 클래스에 붙은 @RequireAccount 확인
    RequireAccount requireAccount =
        handlerMethod.getMethodAnnotation(RequireAccount.class) != null
            ? handlerMethod.getMethodAnnotation(RequireAccount.class)
            : handlerMethod.getBeanType().getAnnotation(RequireAccount.class);

    if (requireAccount == null) {
      return true; // 검사 필요 없음
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      throw new UnauthorizedException("로그인이 필요한 요청입니다.");
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserDetailsImpl userDetails)) {
      throw new UnauthorizedException("인증 정보가 올바르지 않습니다.");
    }

    // ✅ 현재 사용자 accountType과 @RequireAccount 비교
    AccountType required = AccountType.valueOf(requireAccount.value());
    AccountType actual = AccountType.valueOf(userDetails.getAccountType());

    if (!required.equals(actual)) {
      throw new UnauthorizedException("허용되지 않은 계정 유형입니다. (" + actual + ")");
    }

    return true;
  }
}
