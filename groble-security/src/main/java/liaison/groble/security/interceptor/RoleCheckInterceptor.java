package liaison.groble.security.interceptor;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import liaison.groble.common.annotation.RequireRole;
import liaison.groble.security.exception.ForbiddenException;
import liaison.groble.security.exception.UnauthorizedException;

@Component
public class RoleCheckInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    HandlerMethod handlerMethod = (HandlerMethod) handler;
    RequireRole requiredRole = handlerMethod.getMethodAnnotation(RequireRole.class);

    if (requiredRole == null) {
      return true;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("인증 정보가 없습니다.");
    }

    boolean hasRequiredRole =
        Arrays.stream(requiredRole.value())
            .anyMatch(
                role ->
                    authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(role)));

    if (!hasRequiredRole) {
      throw new ForbiddenException("접근 권한이 없습니다.");
    }

    return true;
  }
}
