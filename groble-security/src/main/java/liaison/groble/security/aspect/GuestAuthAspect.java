package liaison.groble.security.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import liaison.groble.common.annotation.GuestOnly;
import liaison.groble.security.exception.ForbiddenException;

@Aspect
@Component
public class GuestAuthAspect {

  @Before("@annotation(guestOnly)")
  public void validateGuestAccess(JoinPoint joinPoint, GuestOnly guestOnly) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // 게스트 사용자인지 확인 (principal이 "guest_ID" 형태인지 확인)
    if (auth == null || auth.getName() == null || !auth.getName().startsWith("guest_")) {
      throw new ForbiddenException(guestOnly.message());
    }
  }
}
