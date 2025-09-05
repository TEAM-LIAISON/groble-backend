package liaison.groble.security.resolver;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.security.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(Auth.class)
        && parameter.getParameterType().equals(Accessor.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {

    Auth authAnnotation = parameter.getParameterAnnotation(Auth.class);
    boolean required = authAnnotation != null ? authAnnotation.required() : true;

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 인증 정보가 없거나 인증되지 않은 경우
    if (authentication == null || !authentication.isAuthenticated()) {
      if (required) {
        throw new UnauthorizedException("인증 정보가 없습니다.");
      } else {
        // Optional 인증인 경우 익명 사용자 Accessor 반환
        return createAnonymousAccessor();
      }
    }

    Object principal = authentication.getPrincipal();

    // 익명 사용자인 경우 (예: "anonymousUser")
    if (principal instanceof String && "anonymousUser".equals(principal)) {
      if (required) {
        throw new UnauthorizedException("인증 정보가 유효하지 않습니다.");
      } else {
        return createAnonymousAccessor();
      }
    }

    // 게스트 사용자인 경우 처리 추가 - GuestPrincipal을 사용하지 않고 직접 처리
    if (authentication.getName() != null && authentication.getName().startsWith("guest_")) {
      String guestIdStr = authentication.getName().replace("guest_", "");
      try {
        Long guestId = Long.parseLong(guestIdStr);

        Set<String> roles =
            authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Accessor.builder()
            .id(guestId)
            .roles(roles)
            .userType("GUEST")
            .accountType("GUEST")
            .build();
      } catch (NumberFormatException e) {
        // 게스트 ID 파싱 실패시 익명 사용자로 처리
        if (required) {
          throw new UnauthorizedException("게스트 인증 정보가 유효하지 않습니다.");
        } else {
          return createAnonymousAccessor();
        }
      }
    }

    // UserDetailsImpl이 아닌 경우
    if (!(principal instanceof liaison.groble.security.jwt.UserDetailsImpl)) {
      if (required) {
        throw new UnauthorizedException("인증 정보가 유효하지 않습니다.");
      } else {
        return createAnonymousAccessor();
      }
    }

    // 인증된 사용자 정보로 Accessor 생성
    liaison.groble.security.jwt.UserDetailsImpl userDetails =
        (liaison.groble.security.jwt.UserDetailsImpl) principal;

    Set<String> roles =
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    String userType = "BUYER"; // 기본값
    if (roles.contains("ROLE_SELLER")) {
      userType = userDetails.getLastUserType() != null ? userDetails.getLastUserType() : "BUYER";
    }

    String accountType = userDetails.getAccountType();

    return Accessor.builder()
        .id(userDetails.getId())
        .email(userDetails.getEmail())
        .roles(roles)
        .userType(userType)
        .accountType(accountType)
        .build();
  }

  /** 익명 사용자를 위한 Accessor 생성 */
  private Accessor createAnonymousAccessor() {
    return Accessor.builder()
        .id(null)
        .email(null)
        .roles(Set.of())
        .userType("ANONYMOUS")
        .accountType(null)
        .build();
  }
}
