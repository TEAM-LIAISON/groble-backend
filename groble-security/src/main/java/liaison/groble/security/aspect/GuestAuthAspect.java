package liaison.groble.security.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import liaison.groble.common.annotation.GuestOnly;
import liaison.groble.security.exception.ForbiddenException;
import liaison.groble.security.jwt.GuestPrincipal;

@Aspect
@Component
public class GuestAuthAspect {

  @Before("@annotation(guestOnly)")
  public void validateGuestAccess(JoinPoint joinPoint, GuestOnly guestOnly) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof GuestPrincipal)) {
      throw new ForbiddenException(guestOnly.message());
    }
  }
}
