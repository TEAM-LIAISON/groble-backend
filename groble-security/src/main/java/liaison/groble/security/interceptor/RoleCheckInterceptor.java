package liaison.groble.security.interceptor;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    String[] requiredRoles = requiredRole.value();
    boolean hasRequiredRole =
        Arrays.stream(requiredRoles)
            .anyMatch(
                role ->
                    authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(role)));

    if (!hasRequiredRole) {
      String requiredRolesStr = Arrays.stream(requiredRoles).collect(Collectors.joining(", "));

      String currentRoles =
          authentication.getAuthorities().stream()
              .map(auth -> auth.getAuthority())
              .collect(Collectors.joining(", "));

      String errorMessage =
          String.format("필요한 권한이 없습니다. 필요한 권한: [%s], 현재 권한: [%s]", requiredRolesStr, currentRoles);

      throw new ForbiddenException(errorMessage);
    }

    return true;
  }
}
